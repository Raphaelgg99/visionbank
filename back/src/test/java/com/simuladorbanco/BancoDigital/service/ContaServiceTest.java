package com.simuladorbanco.BancoDigital.service;

import com.simuladorbanco.BancoDigital.dtos.ContaDTO;
import com.simuladorbanco.BancoDigital.dtos.TransacaoDTO;
import com.simuladorbanco.BancoDigital.dtos.TransferenciaRequest;
import com.simuladorbanco.BancoDigital.exception.EmailNullException;
import com.simuladorbanco.BancoDigital.exception.EmailRepetidoException;
import com.simuladorbanco.BancoDigital.exception.SaldoInsuficienteException;
import com.simuladorbanco.BancoDigital.exception.SenhaNullException;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.model.Transacao;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import com.simuladorbanco.BancoDigital.repository.TransacaoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.yaml.snakeyaml.events.Event;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @InjectMocks
    private ContaService contaService;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    TransacaoService transacaoService;

    @Mock
    private TransacaoRepository transacaoRepository;

    private Conta conta;

    @BeforeEach // Este método será executado antes de cada teste
    void setUp() {
        // Cria um objeto base para ser usado nos testes
        conta = new Conta();
        conta.setNumeroDaConta(1L);
        conta.setEmail("usuario@teste.com");
        conta.setSenha("senha123");
        conta.setSaldo(500);
        conta.setNome("Usuario");
    }


    @Test
    @DisplayName("Deve criar uma conta com sucesso")
    void deveCriarContaComSucesso() {
        // Cenário
        // 1. Garante que não existe conta com o e-mail fornecido
        when(contaRepository.findByEmail(conta.getEmail())).thenReturn(null);
        // 2. Define o comportamento esperado para o encoder
        when(encoder.encode(conta.getSenha())).thenReturn("senhaCriptografada");
        // 3. Define o comportamento esperado para o save (retorna a conta salva)
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        // Ação
        Conta contaSalva = contaService.criarConta(conta);

        // Verificação
        assertNotNull(contaSalva);
        assertEquals("senhaCriptografada", contaSalva.getSenha());

        // Verifica se os métodos dos mocks foram chamados como esperado
        verify(contaRepository).findByEmail(conta.getEmail());
        verify(encoder).encode("senha123");
        verify(contaRepository).save(conta);
    }

    @Test
    @DisplayName("Deve lançar EmailNullException quando o email for nulo")
    void deveLancarEmailNullExceptionQuandoEmailForNulo() {
        // Cenário
        conta.setEmail(null);

        // Ação e Verificação
        assertThrows(EmailNullException.class, () -> {
            contaService.criarConta(conta);
        });

        // Garante que o repositório e o encoder nunca foram chamados
        verify(contaRepository, never()).findByEmail(anyString());
        verify(encoder, never()).encode(anyString());
        verify(contaRepository, never()).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve lançar SenhaNullException quando a senha for nula")
    void deveLancarSenhaNullExceptionQuandoSenhaForNula() {
        // Cenário
        conta.setSenha(null);

        // Ação e Verificação
        assertThrows(SenhaNullException.class, () -> {
            contaService.criarConta(conta);
        });

        // Garante que o repositório e o encoder nunca foram chamados
        verify(contaRepository, never()).findByEmail(anyString());
        verify(encoder, never()).encode(anyString());
        verify(contaRepository, never()).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve lançar EmailRepetidoException quando o email já existir")
    void deveLancarEmailRepetidoExceptionQuandoEmailJaExistir() {
        // Cenário
        // Simula que o repositório encontrou uma conta com o mesmo e-mail
        when(contaRepository.findByEmail(conta.getEmail())).thenReturn(new Conta());

        // Ação e Verificação
        assertThrows(EmailRepetidoException.class, () -> {
            contaService.criarConta(conta);
        });

        // Verifica se o findByEmail foi chamado, mas o encode e o save não
        verify(contaRepository).findByEmail(conta.getEmail());
        verify(encoder, never()).encode(anyString());
        verify(contaRepository, never()).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve atualizar uma conta com sucesso")
    void deveAtualizarContaComSucesso() {
        // Cenário
        Long numeroDaConta = 1L;
        Conta contaExistente = new Conta(); // A conta como ela está no banco
        contaExistente.setNumeroDaConta(numeroDaConta);
        contaExistente.setNome("Nome Antigo");
        contaExistente.setEmail("email@original.com");

        Conta dadosParaAtualizar = new Conta(); // Os novos dados que estão vindo na requisição
        dadosParaAtualizar.setNome("Nome Novo");
        dadosParaAtualizar.setSenha("novaSenha123");
        dadosParaAtualizar.setEmail("email@original.com"); // Mesmo email, não deve dar erro
        dadosParaAtualizar.setSaldo(100.0);

        // Mockando o comportamento dos repositórios e serviços
        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(contaExistente));
        when(encoder.encode(dadosParaAtualizar.getSenha())).thenReturn("senhaCriptografadaNova");
        when(contaRepository.save(any(Conta.class))).thenReturn(contaExistente);

        // Ação
        Conta contaAtualizada = contaService.atualizarConta(numeroDaConta, dadosParaAtualizar);

        // Verificação
        assertNotNull(contaAtualizada);
        assertEquals("Nome Novo", contaAtualizada.getNome());
        assertEquals("senhaCriptografadaNova", contaAtualizada.getSenha());
        assertEquals(100.0, contaAtualizada.getSaldo());

        // Verifica se os métodos foram chamados
        verify(contaRepository).findById(numeroDaConta);
        verify(encoder).encode("novaSenha123");
        verify(contaRepository).save(contaExistente);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao tentar atualizar conta inexistente")
    void deveLancarRuntimeExceptionQuandoContaNaoForEncontrada() {
        // Cenário
        Long numeroDaContaInexistente = 99L;
        Conta dadosParaAtualizar = new Conta();
        // Simula que o repositório não encontrou a conta
        when(contaRepository.findById(numeroDaContaInexistente)).thenReturn(Optional.empty());

        // Ação e Verificação
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contaService.atualizarConta(numeroDaContaInexistente, dadosParaAtualizar);
        });

        assertEquals("Conta não encontrado", exception.getMessage());

        // Garante que outros métodos não foram chamados
        verify(contaRepository, never()).save(any(Conta.class));
        verify(encoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Deve lançar EmailRepetidoException ao tentar usar email de outra conta")
    void deveLancarEmailRepetidoExceptionQuandoNovoEmailJaExiste() {
        // Cenário
        Long numeroDaConta = 1L;
        String emailOriginal = "usuario1@teste.com";
        String emailRepetido = "usuario2@teste.com";

        Conta contaExistente = new Conta(); // A conta do usuário 1
        contaExistente.setNumeroDaConta(numeroDaConta);
        contaExistente.setEmail(emailOriginal);

        Conta dadosParaAtualizar = new Conta(); // Tentando atualizar com o e-mail do usuário 2
        dadosParaAtualizar.setEmail(emailRepetido);
        dadosParaAtualizar.setSenha("qualquersenha");

        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(contaExistente));
        // Simula que o novo email (emailRepetido) já existe no banco
        when(contaRepository.existsByEmail(emailRepetido)).thenReturn(true);

        // Ação e Verificação
        assertThrows(EmailRepetidoException.class, () -> {
            contaService.atualizarConta(numeroDaConta, dadosParaAtualizar);
        });

        // Garante que a conta não foi salva
        verify(contaRepository, never()).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve atualizar conta com sucesso ao mudar para um email novo e válido")
    void deveAtualizarContaComSucessoQuandoMudaParaEmailNaoExistente() {
        // Cenário
        Long numeroDaConta = 1L;
        Conta contaExistente = new Conta();
        contaExistente.setNumeroDaConta(numeroDaConta);
        contaExistente.setEmail("email@antigo.com");

        Conta dadosParaAtualizar = new Conta();
        dadosParaAtualizar.setEmail("email@novo.com"); // Um email novo
        dadosParaAtualizar.setSenha("senhaNova");
        dadosParaAtualizar.setNome("Nome");
        dadosParaAtualizar.setSaldo(200.0);

        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(contaExistente));
        // Simula que o novo e-mail NÃO existe no banco
        when(contaRepository.existsByEmail("email@novo.com")).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("senhaCriptografada");
        when(contaRepository.save(any(Conta.class))).thenReturn(contaExistente);


        // Ação
        Conta contaAtualizada = contaService.atualizarConta(numeroDaConta, dadosParaAtualizar);


        // Verificação
        assertNotNull(contaAtualizada);
        assertEquals("email@novo.com", contaAtualizada.getEmail());
        verify(contaRepository).save(contaExistente);
    }

    @Test
    @DisplayName("Deve depositar valor com sucesso em uma conta existente")
    void deveDepositarComSucesso() {
        // Cenário (Arrange)
        Long numeroDaConta = 1L;
        Double valorDeposito = 100.0;
        Double saldoInicial = 50.0;

        // Criamos uma conta que simula o estado ANTES do depósito
        Conta conta = new Conta();
        conta.setNumeroDaConta(numeroDaConta);
        conta.setSaldo(saldoInicial);

        // Criamos os objetos de transação que esperamos que sejam retornados
        Transacao transacao = new Transacao(); // Preencha com dados se necessário
        TransacaoDTO transacaoDTOEsperado = new TransacaoDTO(); // Preencha com dados se necessário

        // Mockando o comportamento dos repositórios e serviços
        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(conta));
        // Não precisamos mockar o save, pois ele retorna void. O verify fará o trabalho.
        when(transacaoService.adicionarTransacaoDeposito(any(Conta.class), anyDouble())).thenReturn(transacao);
        when(transacaoService.adicionarTransacaoDTODeposito(any(Conta.class), any(Transacao.class))).thenReturn(transacaoDTOEsperado);

        // Ação (Act)
        TransacaoDTO resultadoDTO = contaService.depositar(valorDeposito, numeroDaConta);

        // Verificação (Assert)
        assertNotNull(resultadoDTO, "O DTO retornado не должен ser nulo.");
        assertEquals(transacaoDTOEsperado, resultadoDTO, "O DTO de transação retornado deve ser o esperado.");

        // Verifica se o saldo foi atualizado corretamente ANTES de salvar
        assertEquals(saldoInicial + valorDeposito, conta.getSaldo(), "O saldo da conta deveria ter sido atualizado.");

        // Verifica se os métodos corretos foram chamados com os parâmetros corretos
        verify(contaRepository).findById(numeroDaConta);
        verify(contaRepository).save(conta); // Verifica se o método save foi chamado com o objeto conta modificado
        verify(transacaoService).adicionarTransacaoDeposito(conta, valorDeposito);
        verify(transacaoService).adicionarTransacaoDTODeposito(conta, transacao);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao tentar depositar em conta inexistente")
    void deveLancarRuntimeExceptionAoDepositarEmContaInexistente() {
        // Cenário
        Long numeroDaContaInexistente = 99L;
        Double valorDeposito = 100.0;
        when(contaRepository.findById(numeroDaContaInexistente)).thenReturn(Optional.empty());

        // Ação e Verificação
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contaService.depositar(valorDeposito, numeroDaContaInexistente);
        });

        assertEquals("Conta não encontrada", exception.getMessage());
        verify(contaRepository, never()).save(any(Conta.class));
        verify(transacaoService, never()).adicionarTransacaoDeposito(any(), anyDouble());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para valor de depósito nulo")
    void deveLancarIllegalArgumentExceptionParaDepositoNulo() {
        // Cenário
        Long numeroDaConta = 1L;
        // Mockamos o findById para isolar o teste na validação do valor
        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(new Conta()));

        // Ação e Verificação
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.depositar(null, numeroDaConta);
        });

        assertEquals("O valor do depósito deve ser maior que zero.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para valor de depósito igual a zero")
    void deveLancarIllegalArgumentExceptionParaDepositoZero() {
        // Cenário
        Long numeroDaConta = 1L;
        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(new Conta()));

        // Ação e Verificação
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.depositar(0.0, numeroDaConta);
        });

        assertEquals("O valor do depósito deve ser maior que zero.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para valor de depósito negativo")
    void deveLancarIllegalArgumentExceptionParaDepositoNegativo() {
        // Cenário
        Long numeroDaConta = 1L;
        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(new Conta()));

        // Ação e Verificação
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.depositar(-50.0, numeroDaConta);
        });

        assertEquals("O valor do depósito deve ser maior que zero.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve sacar valor com sucesso em uma conta existente")
    void deveSacarComSucesso() {
        // Cenário (Arrange)
        Long numeroDaConta = 1L;
        Double valorSaque = 100.0;
        Double saldoInicial = 250.0;

        // Criamos uma conta que simula o estado ANTES do depósito
        Conta conta = new Conta();
        conta.setNumeroDaConta(numeroDaConta);
        conta.setSaldo(saldoInicial);

        // Criamos os objetos de transação que esperamos que sejam retornados
        Transacao transacao = new Transacao(); // Preencha com dados se necessário
        TransacaoDTO transacaoDTOEsperado = new TransacaoDTO(); // Preencha com dados se necessário

        // Mockando o comportamento dos repositórios e serviços
        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(conta));
        // Não precisamos mockar o save, pois ele retorna void. O verify fará o trabalho.
        when(transacaoService.adicionarTransacaoSaque(any(Conta.class), anyDouble())).thenReturn(transacao);
        when(transacaoService.adicionarTransacaoDTOSaque(any(Conta.class), any(Transacao.class))).thenReturn(transacaoDTOEsperado);

        // Ação (Act)
        TransacaoDTO resultadoDTO = contaService.sacar(valorSaque, numeroDaConta);

        // Verificação (Assert)
        assertNotNull(resultadoDTO);
        assertEquals(transacaoDTOEsperado, resultadoDTO);

        // Verifica se o saldo foi atualizado corretamente ANTES de salvar
        assertEquals(saldoInicial - valorSaque, conta.getSaldo());

        // Verifica se os métodos corretos foram chamados com os parâmetros corretos
        verify(contaRepository).findById(numeroDaConta);
        verify(contaRepository).save(conta); // Verifica se o método save foi chamado com o objeto conta modificado
        verify(transacaoService).adicionarTransacaoSaque(conta, valorSaque);
        verify(transacaoService).adicionarTransacaoDTOSaque(conta, transacao);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao tentar sacar em conta inexistente")
    void deveLancarRuntimeExceptionAoSacarEmContaInexistente() {
        // Cenário
        Long numeroDaContaInexistente = 99L;
        Double valorSaque = 100.0;
        when(contaRepository.findById(numeroDaContaInexistente)).thenReturn(Optional.empty());

        // Ação e Verificação
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contaService.sacar(valorSaque, numeroDaContaInexistente);
        });

        assertEquals("Conta não encontrada", exception.getMessage());
        verify(contaRepository, never()).save(any(Conta.class));
        verify(transacaoService, never()).adicionarTransacaoSaque(any(), anyDouble());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para valor de saque igual a zero")
    void deveLancarIllegalArgumentExceptionParaSaqueZero() {
        // Cenário
        Long numeroDaConta = 1L;
        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(new Conta()));

        // Ação e Verificação
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.sacar(0.0, numeroDaConta);
        });

        assertEquals("O valor do saque deve ser maior que zero.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para valor de saque negativo")
    void deveLancarIllegalArgumentExceptionParaSaqueNegativo() {
        // Cenário
        Long numeroDaConta = 1L;
        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(new Conta()));

        // Ação e Verificação
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contaService.sacar(-50.0, numeroDaConta);
        });

        assertEquals("O valor do saque deve ser maior que zero.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve realizar transferência com sucesso entre duas contas existentes")
    void deveRealizarTransferenciaComSucesso() {
        // Cenário (Arrange)
        Long numeroContaRemetente = 1L;
        Long numeroContaDestinatario = 2L;
        Double saldoInicialRemetente = 500.0;
        Double saldoInicialDestinatario = 200.0;
        Double valorTransferencia = 100.0;

        // Criando o objeto de requisição da transferência
        TransferenciaRequest request = new TransferenciaRequest();
        request.setNumeroContaDestinatario(numeroContaDestinatario);
        request.setValor(valorTransferencia);

        // Criando as contas que simulam o estado no banco
        Conta remetente = new Conta();
        remetente.setNumeroDaConta(numeroContaRemetente);
        remetente.setSaldo(saldoInicialRemetente);

        Conta destinatario = new Conta();
        destinatario.setNumeroDaConta(numeroContaDestinatario);
        destinatario.setSaldo(saldoInicialDestinatario);

        // Mockando os retornos do repositório
        when(contaRepository.findById(numeroContaRemetente)).thenReturn(Optional.of(remetente));
        when(contaRepository.findById(numeroContaDestinatario)).thenReturn(Optional.of(destinatario));

        // Mockando os retornos do serviço de transação
        TransacaoDTO transacaoDTOEsperado = new TransacaoDTO();
        when(transacaoService.adicionarTransacaoTransferencia(any(), any(), anyDouble())).thenReturn(new Transacao());
        when(transacaoService.adicionarTransacaoDTOTransferencia(any(), any(), any())).thenReturn(transacaoDTOEsperado);

        // Ação (Act)
        TransacaoDTO resultadoDTO = contaService.transferencia(request, numeroContaRemetente);

        // Verificação (Assert)
        assertNotNull(resultadoDTO);
        assertEquals(transacaoDTOEsperado, resultadoDTO);

        // Verifica se os saldos foram alterados corretamente
        assertEquals(400.0, remetente.getSaldo(), "O saldo do remetente deve ser debitado.");
        assertEquals(300.0, destinatario.getSaldo(), "O saldo do destinatário deve ser creditado.");

        // Verifica se o método save foi chamado para AMBAS as contas
        verify(contaRepository, times(2)).save(any(Conta.class));
        verify(contaRepository).save(remetente);
        verify(contaRepository).save(destinatario);

        // Verifica se os métodos de transação foram chamados
        verify(transacaoService).adicionarTransacaoTransferencia(eq(remetente), eq(destinatario), eq(valorTransferencia));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando a conta do remetente não é encontrada")
    void deveLancarExcecaoQuandoRemetenteNaoEncontrado() {
        // Cenário
        Long numeroContaRemetente = 99L; // Conta inexistente
        TransferenciaRequest request = new TransferenciaRequest();
        request.setNumeroContaDestinatario(2L);
        request.setValor(100.0);

        when(contaRepository.findById(numeroContaRemetente)).thenReturn(Optional.empty());

        // Ação e Verificação
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contaService.transferencia(request, numeroContaRemetente);
        });

        assertEquals("Conta não encontrada", exception.getMessage());
        verify(contaRepository, never()).save(any(Conta.class));
        verify(transacaoService, never()).adicionarTransacaoTransferencia(any(), any(), anyDouble());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando a conta do destinatário não é encontrada")
    void deveLancarExcecaoQuandoDestinatarioNaoEncontrado() {
        // Cenário
        Long numeroContaRemetente = 1L;
        Long numeroContaDestinatario = 99L; // Conta inexistente

        TransferenciaRequest request = new TransferenciaRequest();
        request.setNumeroContaDestinatario(numeroContaDestinatario);
        request.setValor(100.0);

        // Remetente existe
        when(contaRepository.findById(numeroContaRemetente)).thenReturn(Optional.of(new Conta()));
        // Destinatário não existe
        when(contaRepository.findById(numeroContaDestinatario)).thenReturn(Optional.empty());

        // Ação e Verificação
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contaService.transferencia(request, numeroContaRemetente);
        });

        assertEquals("Conta não encontrada", exception.getMessage());
        verify(contaRepository, never()).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve lançar SaldoInsuficienteException quando o remetente не имеет saldo suficiente")
    void deveLancarSaldoInsuficienteException() {
        // Cenário
        Long numeroContaRemetente = 1L;
        Long numeroContaDestinatario = 2L;

        TransferenciaRequest request = new TransferenciaRequest();
        request.setNumeroContaDestinatario(numeroContaDestinatario);
        request.setValor(500.0); // Tenta transferir 500

        Conta remetente = new Conta();
        remetente.setNumeroDaConta(numeroContaRemetente);
        remetente.setSaldo(100.0); // Mas só tem 100 de saldo

        Conta destinatario = new Conta();
        destinatario.setNumeroDaConta(numeroContaDestinatario);

        when(contaRepository.findById(numeroContaRemetente)).thenReturn(Optional.of(remetente));
        when(contaRepository.findById(numeroContaDestinatario)).thenReturn(Optional.of(destinatario));

        // Ação e Verificação
        assertThrows(SaldoInsuficienteException.class, () -> {
            contaService.transferencia(request, numeroContaRemetente);
        });

        // Garante que nenhuma alteração foi salva no banco
        verify(contaRepository, never()).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve remover uma conta e reassociar suas transações com sucesso")
    void deveRemoverContaComSucesso() {
        // Cenário (Arrange)
        Long numeroContaParaRemover = 1L;
        Long numeroContaDefault = 99L;

        Conta contaParaRemover = new Conta();
        contaParaRemover.setNumeroDaConta(numeroContaParaRemover);

        Conta contaDefault = new Conta();
        contaDefault.setNumeroDaConta(numeroContaDefault);

        // Criamos mocks de transações para verificar se elas são atualizadas
        Transacao transacaoRemetente = mock(Transacao.class);
        Transacao transacaoDestinatario = mock(Transacao.class);

        List<Transacao> listaRemetente = Arrays.asList(transacaoRemetente);
        List<Transacao> listaDestinatario = Arrays.asList(transacaoDestinatario);

        // Mockando os comportamentos dos repositórios
        when(contaRepository.findById(numeroContaParaRemover)).thenReturn(Optional.of(contaParaRemover));
        when(contaRepository.findById(numeroContaDefault)).thenReturn(Optional.of(contaDefault));
        when(transacaoRepository.findByContaRemetente(contaParaRemover)).thenReturn(listaRemetente);
        when(transacaoRepository.findByContaDestinatario(contaParaRemover)).thenReturn(listaDestinatario);

        // Ação (Act)
        contaService.removerConta(numeroContaParaRemover);

        // Verificação (Assert)
        // Verifica se as buscas pelas contas foram feitas
        verify(contaRepository).findById(numeroContaParaRemover);
        verify(contaRepository).findById(numeroContaDefault);

        // Verifica se as buscas por transações foram feitas
        verify(transacaoRepository).findByContaRemetente(contaParaRemover);
        verify(transacaoRepository).findByContaDestinatario(contaParaRemover);

        // Verifica se as transações foram reassociadas para a conta default
        verify(transacaoRemetente).setContaRemetente(contaDefault);
        verify(transacaoDestinatario).setContaDestinatario(contaDefault);

        // Verifica se a conta foi finalmente deletada
        verify(contaRepository).delete(contaParaRemover);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao tentar remover uma conta que não existe")
    void deveLancarExcecaoQuandoContaARemoverNaoEncontrada() {
        // Cenário
        Long numeroContaInexistente = 99L;
        when(contaRepository.findById(numeroContaInexistente)).thenReturn(Optional.empty());

        // Ação e Verificação
        assertThrows(RuntimeException.class, () -> {
            contaService.removerConta(numeroContaInexistente);
        });

        // Garante que nenhuma ação de exclusão ou busca de transações ocorreu
        verify(contaRepository, never()).delete(any(Conta.class));
        verify(transacaoRepository, never()).findByContaRemetente(any(Conta.class));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException se a conta default (99L) não for encontrada")
    void deveLancarExcecaoQuandoContaDefaultNaoEncontrada() {
        // Cenário
        Long numeroContaParaRemover = 1L;
        Long numeroContaDefault = 99L;

        Conta contaParaRemover = new Conta(); // A conta a ser removida existe...
        when(contaRepository.findById(numeroContaParaRemover)).thenReturn(Optional.of(contaParaRemover));

        // ...mas a conta default não existe
        when(contaRepository.findById(numeroContaDefault)).thenReturn(Optional.empty());

        // Ação e Verificação
        assertThrows(RuntimeException.class, () -> {
            contaService.removerConta(numeroContaParaRemover);
        });

        // Garante que a conta original não foi deletada
        verify(contaRepository, never()).delete(any(Conta.class));
    }

    @Test
    @DisplayName("Deve remover conta sem transações com sucesso")
    void deveRemoverContaSemTransacoesComSucesso() {
        // Cenário
        Long numeroContaParaRemover = 1L;
        Long numeroContaDefault = 99L;

        Conta contaParaRemover = new Conta();
        contaParaRemover.setNumeroDaConta(numeroContaParaRemover);

        Conta contaDefault = new Conta();
        contaDefault.setNumeroDaConta(numeroContaDefault);

        // Simula que a conta não participou de nenhuma transação
        when(contaRepository.findById(numeroContaParaRemover)).thenReturn(Optional.of(contaParaRemover));
        when(contaRepository.findById(numeroContaDefault)).thenReturn(Optional.of(contaDefault));
        when(transacaoRepository.findByContaRemetente(contaParaRemover)).thenReturn(Collections.emptyList());
        when(transacaoRepository.findByContaDestinatario(contaParaRemover)).thenReturn(Collections.emptyList());

        // Ação
        contaService.removerConta(numeroContaParaRemover);

        // Verificação
        // Apenas verifica se a conta foi deletada, já que não há transações para alterar
        verify(contaRepository).delete(contaParaRemover);
    }

    @Test
    @DisplayName("Deve retornar uma lista de todas as contas com sucesso")
    void deveRetornarTodasAsContasComSucesso() {
        // Cenário (Arrange)
        // Cria duas contas para simular o que estaria no banco de dados
        Conta conta1 = new Conta();
        conta1.setNumeroDaConta(1L);
        conta1.setEmail("usuario1@email.com");

        Conta conta2 = new Conta();
        conta2.setNumeroDaConta(2L);
        conta2.setEmail("usuario2@email.com");

        List<Conta> listaDeContas = Arrays.asList(conta1, conta2);

        // Mocka o comportamento do repositório para retornar a lista criada
        when(contaRepository.findAll()).thenReturn(listaDeContas);

        // Ação (Act)
        List<Conta> contasRetornadas = contaService.listarTodos();

        // Verificação (Assert)
        assertNotNull(contasRetornadas, "A lista retornada não deveria ser nula.");
        assertEquals(2, contasRetornadas.size(), "O tamanho da lista deveria ser 2.");
        assertEquals(listaDeContas, contasRetornadas, "A lista retornada deveria ser a mesma que a do repositório.");

        // Verifica se o método findAll do repositório foi chamado exatamente uma vez
        verify(contaRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando não houver contas")
    void deveRetornarListaVaziaQuandoNaoHouverContas() {
        // Cenário (Arrange)
        // Mocka o comportamento do repositório para retornar uma lista vazia
        when(contaRepository.findAll()).thenReturn(Collections.emptyList());

        // Ação (Act)
        List<Conta> contasRetornadas = contaService.listarTodos();

        // Verificação (Assert)
        assertNotNull(contasRetornadas, "A lista retornada não deveria ser nula.");
        assertTrue(contasRetornadas.isEmpty(), "A lista retornada deveria estar vazia.");

        verify(contaRepository).findAll();
    }





}





