package com.simuladorbanco.BancoDigital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simuladorbanco.BancoDigital.config.JWTCreator;
import com.simuladorbanco.BancoDigital.config.JWTObject;
import com.simuladorbanco.BancoDigital.config.SecurityConfig;
import com.simuladorbanco.BancoDigital.dtos.TransacaoDTO;
import com.simuladorbanco.BancoDigital.dtos.TransferenciaRequest;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.model.Transacao;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import com.simuladorbanco.BancoDigital.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.is;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // 1. Carrega a aplicação COMPLETA
@AutoConfigureMockMvc // 2. Configura o MockMvc para fazer as requisições
@ActiveProfiles("test") // 3. Ativa o application-test.properties que criamos
@Transactional // 4. A MÁGICA: Cada teste roda em uma transação que é revertida no final
class ContaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired // 5. Injetamos o repositório REAL para preparar o banco de dados
    private ContaRepository contaRepository;

    @Autowired // Precisamos do TransacaoRepository para criar os dados de teste
    private TransacaoRepository transacaoRepository;

    private Conta contaSalvaNoBanco;

    @BeforeEach
    void setUp() {
        // 6. Antes de CADA teste, criamos uma conta e a salvamos no banco H2
        Conta conta = new Conta();
        conta.setNome("Cliente Teste");
        conta.setEmail("teste@email.com");
        conta.setSenha("senhaCriptografada");
        conta.setSaldo(500.0);
        conta.setRoles(new ArrayList<>(Arrays.asList("ADMIN")));;
        contaSalvaNoBanco = contaRepository.save(conta);
        SecurityConfig.PREFIX = "Bearer";
        SecurityConfig.KEY = "uma-chave-secreta-muito-longa-para-testes-de-seguranca";
        SecurityConfig.EXPIRATION = 3600000L; // 1 hora
    }

    @Test
    @DisplayName("Deve depositar 100 em uma conta com saldo 500 e o saldo final deve ser 600")
    void deveDepositarComSucessoEAtualizarSaldoNoBanco() throws Exception {
        // Cenário (Arrange)
        double valorDeposito = 100.0;

        JWTObject jwtObject = new JWTObject();
        jwtObject.setSubject(contaSalvaNoBanco.getEmail()); // O "username" do token
        jwtObject.setIssuedAt(new Date(System.currentTimeMillis()));
        jwtObject.setExpiration(new Date(System.currentTimeMillis() +  SecurityConfig.EXPIRATION)); // Válido por 1 hora
        jwtObject.setRoles(contaSalvaNoBanco.getRoles());

        // 2. Geramos a string do token
        String token = JWTCreator.create(SecurityConfig.PREFIX , SecurityConfig.KEY, jwtObject);
        // --- FIM DO BLOCO DE CRIAÇÃO DO TOKEN ---

        // Ação (Act) e Verificação (Assert) da API
        mockMvc.perform(put("/conta/{numeroDaConta}/depositar", contaSalvaNoBanco.getNumeroDaConta())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valorDeposito)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(100.0));

        // Verificação extra: Buscamos a conta diretamente no banco para confirmar o efeito colateral
        Conta contaDoBancoAposDeposito = contaRepository.findById(contaSalvaNoBanco.getNumeroDaConta()).get();
        assertEquals(600.0, contaDoBancoAposDeposito.getSaldo(), "O saldo no banco deveria ser atualizado para 600.");
    }

    @Test
    @DisplayName("Deve sacar 100 de uma conta com saldo 500 e o saldo final deve ser 400")
    void deveSacarComSucessoEAtualizarSaldoNoBanco() throws Exception {
        // Cenário (Arrange)
        double valorSaque = 100.0;

        // 1. Criamos o token JWT para autenticar nossa requisição
        JWTObject jwtObject = new JWTObject();
        jwtObject.setSubject(contaSalvaNoBanco.getEmail());
        jwtObject.setIssuedAt(new Date(System.currentTimeMillis()));
        jwtObject.setExpiration(new Date(System.currentTimeMillis() + SecurityConfig.EXPIRATION));
        jwtObject.setRoles(contaSalvaNoBanco.getRoles());

        String token = JWTCreator.create(SecurityConfig.PREFIX, SecurityConfig.KEY, jwtObject);

        // Ação (Act) e Verificação (Assert) da API
        mockMvc.perform(put("/conta/{numeroDaConta}/sacar", contaSalvaNoBanco.getNumeroDaConta())
                        .header("Authorization", token) // Adicionamos o token de autorização
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valorSaque)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("SAQUE")) // Verificamos a resposta da API
                .andExpect(jsonPath("$.valor").value(100.0));

        // Verificação extra e MAIS IMPORTANTE: Buscamos a conta no banco para confirmar o efeito colateral
        Conta contaDoBancoAposSaque = contaRepository.findById(contaSalvaNoBanco.getNumeroDaConta()).get();
        assertEquals(400.0, contaDoBancoAposSaque.getSaldo(), "O saldo no banco deveria ser atualizado para 400.");
    }


    @Test
    @DisplayName("Deve transferir 150 de uma conta com saldo 500 para outra com saldo 200, atualizando ambos os saldos")
    void deveTransferirComSucessoEAtualizarSaldosNoBanco() throws Exception {
        // Cenário (Arrange)
        // O remetente já foi criado no @BeforeEach (contaSalvaNoBanco com saldo 500)

        // 1. Criamos e salvamos a conta do DESTINATÁRIO no banco de dados
        Conta contaDestinatario = new Conta();
        contaDestinatario.setNome("Favorecido Teste");
        contaDestinatario.setEmail("destinatario@email.com");
        contaDestinatario.setSenha("outraSenha");
        contaDestinatario.setSaldo(200.0); // Saldo inicial do destinatário
        contaDestinatario.setRoles(new ArrayList<>(Arrays.asList("USER")));
        Conta contaDestinatarioSalva = contaRepository.save(contaDestinatario);

        // 2. Preparamos os dados da transferência
        double valorTransferencia = 150.0;
        TransferenciaRequest requestBody = new TransferenciaRequest();
        requestBody.setNumeroContaDestinatario(contaDestinatarioSalva.getNumeroDaConta());
        requestBody.setValor(valorTransferencia);

        // 3. Criamos o token JWT para o REMETENTE
        JWTObject jwtObject = new JWTObject();
        jwtObject.setSubject(contaSalvaNoBanco.getEmail());
        jwtObject.setIssuedAt(new Date(System.currentTimeMillis()));
        jwtObject.setExpiration(new Date(System.currentTimeMillis() + SecurityConfig.EXPIRATION));
        jwtObject.setRoles(contaSalvaNoBanco.getRoles());
        String token = JWTCreator.create(SecurityConfig.PREFIX, SecurityConfig.KEY, jwtObject);

        // Ação (Act) e Verificação (Assert) da API
        mockMvc.perform(put("/conta/{numeroRemetente}/transferencia", contaSalvaNoBanco.getNumeroDaConta())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$.valor").value(150.0));

        // Verificação final e MAIS IMPORTANTE: checamos os saldos de AMBAS as contas no banco
        Conta remetenteAposTransferencia = contaRepository.findById(contaSalvaNoBanco.getNumeroDaConta()).get();
        Conta destinatarioAposTransferencia = contaRepository.findById(contaDestinatarioSalva.getNumeroDaConta()).get();

        assertEquals(350.0, remetenteAposTransferencia.getSaldo(), "O saldo do remetente deveria ser 350 (500 - 150).");
        assertEquals(350.0, destinatarioAposTransferencia.getSaldo(), "O saldo do destinatário deveria ser 350 (200 + 150).");
    }

    @Test
    @DisplayName("Deve criar uma nova conta com sucesso via API e persistir no banco")
    void deveCriarNovaContaComSucessoNoBanco() throws Exception {
        // Cenário (Arrange)
        // 1. Criamos o objeto que será enviado no corpo da requisição
        Conta novaConta = new Conta();
        novaConta.setNome("Cliente Novo");
        novaConta.setEmail("novo@email.com");
        novaConta.setSenha("senha123"); // Em uma aplicação real, o service criptografaria isso
        novaConta.setRoles(new ArrayList<>(Arrays.asList("USER")));

        // Ação (Act) e Verificação (Assert) da API
        mockMvc.perform(post("/conta/adicionar") // Método POST, sem cabeçalho de autorização
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novaConta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroDaConta", notNullValue())) // Verificamos que um ID foi gerado
                .andExpect(jsonPath("$.nome").value("Cliente Novo"))
                .andExpect(jsonPath("$.email").value("novo@email.com"));

        // Verificação final no banco de dados:
        // Buscamos a conta pelo e-mail para garantir que ela foi realmente salva
        Conta contaSalva = contaRepository.findByEmail("novo@email.com");
        assertNotNull(contaSalva, "A conta deveria ter sido salva no banco de dados.");
        assertEquals("Cliente Novo", contaSalva.getNome());
    }

    @Test
    @DisplayName("Deve atualizar os dados de uma conta existente e persistir no banco")
    void deveAtualizarContaComSucessoNoBanco() throws Exception {
        // Cenário (Arrange)
        // A conta a ser atualizada já foi criada no @BeforeEach (contaSalvaNoBanco)

        // 1. Criamos o objeto com os dados atualizados que serão enviados no corpo da requisição
        Conta dadosAtualizados = new Conta();
        dadosAtualizados.setNome("Nome Cliente Atualizado");
        dadosAtualizados.setEmail("email.atualizado@teste.com");
        dadosAtualizados.setSenha("novaSenha123"); // O service deve lidar com a criptografia

        // 2. Geramos o token JWT para autenticar a requisição
        JWTObject jwtObject = new JWTObject();
        jwtObject.setSubject(contaSalvaNoBanco.getEmail());
        jwtObject.setIssuedAt(new Date(System.currentTimeMillis()));
        jwtObject.setExpiration(new Date(System.currentTimeMillis() + SecurityConfig.EXPIRATION));
        jwtObject.setRoles(contaSalvaNoBanco.getRoles());
        String token = JWTCreator.create(SecurityConfig.PREFIX, SecurityConfig.KEY, jwtObject);

        // Ação (Act) e Verificação (Assert) da API
        mockMvc.perform(put("/conta/{numeroDaConta}/atualizar", contaSalvaNoBanco.getNumeroDaConta())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosAtualizados)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroDaConta").value(contaSalvaNoBanco.getNumeroDaConta()))
                .andExpect(jsonPath("$.nome").value("Nome Cliente Atualizado"))
                .andExpect(jsonPath("$.email").value("email.atualizado@teste.com"));

        // Verificação final no banco de dados:
        // Buscamos a conta novamente para garantir que os dados foram realmente atualizados
        Conta contaDoBancoAposUpdate = contaRepository.findById(contaSalvaNoBanco.getNumeroDaConta()).get();
        assertEquals("Nome Cliente Atualizado", contaDoBancoAposUpdate.getNome());
        assertEquals("email.atualizado@teste.com", contaDoBancoAposUpdate.getEmail());
    }

    @Test
    @DisplayName("Deve listar todas as contas cadastradas e retornar status 200")
    void deveListarTodasAsContasComSucesso() throws Exception {
        // Cenário (Arrange)
        // A primeira conta (contaSalvaNoBanco) já foi criada no @BeforeEach.
        // Vamos criar uma segunda conta para ter uma lista mais completa.
        Conta conta2 = new Conta();
        conta2.setNome("Segundo Cliente");
        conta2.setEmail("segundo@email.com");
        conta2.setSenha("senha456");
        conta2.setRoles(new ArrayList<>(Arrays.asList("USER")));
        contaRepository.save(conta2);

        // Geramos o token JWT para autenticar a requisição
        JWTObject jwtObject = new JWTObject();
        jwtObject.setSubject(contaSalvaNoBanco.getEmail());
        jwtObject.setIssuedAt(new Date(System.currentTimeMillis()));
        jwtObject.setExpiration(new Date(System.currentTimeMillis() + SecurityConfig.EXPIRATION));
        jwtObject.setRoles(contaSalvaNoBanco.getRoles());
        String token = JWTCreator.create(SecurityConfig.PREFIX, SecurityConfig.KEY, jwtObject);

        // Ação (Act) e Verificação (Assert) da API
        mockMvc.perform(get("/conta/listartodas")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                // Verificamos se a resposta é um array JSON com 3 elementos
                // (a conta default, a do setUp e a que criamos neste teste)
                .andExpect(jsonPath("$", hasSize(2)))
                // Verificamos alguns dados do primeiro elemento retornado para confirmar a estrutura
                .andExpect(jsonPath("$[0].nome").exists())
                .andExpect(jsonPath("$[0].email").exists());
    }

    @Test
    @DisplayName("Deve buscar o extrato de uma conta e retornar a lista de transações com status 200")
    void deveBuscarExtratoComSucesso() throws Exception {
        // === Cenário (Arrange) ===
        // A conta principal (contaSalvaNoBanco) já foi criada no @BeforeEach.
        // Vamos criar algumas transações para esta conta para tornar o extrato interessante.

        // 1. Um depósito feito ontem
        Transacao deposito = new Transacao();
        deposito.setTipo("DEPOSITO");
        deposito.setValor(500.0);
        deposito.setContaDestinatario(contaSalvaNoBanco);
        deposito.setData(LocalDateTime.now().minusDays(1)); // Ocorreu ontem
        transacaoRepository.save(deposito);

        // 2. Um saque feito hoje
        Transacao saque = new Transacao();
        saque.setTipo("SAQUE");
        saque.setValor(100.0);
        saque.setContaRemetente(contaSalvaNoBanco);
        saque.setData(LocalDateTime.now()); // Ocorreu hoje
        transacaoRepository.save(saque);

        // 3. Precisamos dar um "flush" no repositório para garantir que as transações sejam salvas antes do teste rodar
        transacaoRepository.flush();

        // 4. Gera o token JWT para o dono da conta
        JWTObject jwtObject = new JWTObject();
        jwtObject.setSubject(contaSalvaNoBanco.getEmail());
        jwtObject.setIssuedAt(new Date(System.currentTimeMillis()));
        jwtObject.setExpiration(new Date(System.currentTimeMillis() + SecurityConfig.EXPIRATION));
        jwtObject.setRoles(contaSalvaNoBanco.getRoles());
        String token = JWTCreator.create(SecurityConfig.PREFIX, SecurityConfig.KEY, jwtObject);

        // === Ação (Act) & Verificação (Assert) ===
        mockMvc.perform(get("/conta/{numeroDaConta}/extrato", contaSalvaNoBanco.getNumeroDaConta())
                .header("Authorization", token))
                .andExpect(status().isOk())
                // O serviço deve retornar um array JSON com as 2 transações que criamos
                .andExpect(jsonPath("$", hasSize(2)))
                // Como o serviço ordena por data decrescente, o saque deve ser o primeiro elemento
                .andExpect(jsonPath("$[0].tipo", is("SAQUE")))
                .andExpect(jsonPath("$[0].valor", is(100.0)))
                // O depósito deve ser o segundo elemento
                .andExpect(jsonPath("$[1].tipo", is("DEPOSITO")))
                .andExpect(jsonPath("$[1].valor", is(500.0)));
    }


}