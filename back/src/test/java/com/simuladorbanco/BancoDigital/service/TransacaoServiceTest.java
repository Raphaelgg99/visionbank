package com.simuladorbanco.BancoDigital.service;

import com.simuladorbanco.BancoDigital.dtos.TransacaoDTO;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.model.Transacao;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import com.simuladorbanco.BancoDigital.repository.TransacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock // Mock para o repositório de transações
    private TransacaoRepository transacaoRepository;

    // Mesmo não sendo usado neste método, é uma dependência da classe e precisa ser mockado
    @Mock
    private ContaRepository contaRepository;

    @InjectMocks // Injeta os mocks na instância do serviço que vamos testar
    private TransacaoService transacaoService;

    @Test
    @DisplayName("Deve adicionar uma transação de SAQUE com os dados corretos")
    void deveAdicionarTransacaoDeSaqueComSucesso() {
        // Cenário (Arrange)
        Conta conta = new Conta();
        conta.setNumeroDaConta(1L);
        double valorSaque = 150.0;

        // Preparamos o objeto que esperamos que o save retorne
        Transacao transacaoSalva = new Transacao();
        transacaoSalva.setId(1L); // Simulando um ID gerado pelo banco

        // Mockamos a chamada ao repositório. Quando save for chamado com qualquer Transacao, retorne transacaoSalva
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacaoSalva);

        // Criamos um ArgumentCaptor para a classe Transacao
        ArgumentCaptor<Transacao> transacaoArgumentCaptor = ArgumentCaptor.forClass(Transacao.class);

        // Ação (Act)
        Transacao resultado = transacaoService.adicionarTransacaoSaque(conta, valorSaque);

        // Verificação (Assert)

        // 1. Verificamos se o método save foi chamado e capturamos o argumento passado para ele
        verify(transacaoRepository).save(transacaoArgumentCaptor.capture());

        // 2. Pegamos o objeto que foi capturado
        Transacao transacaoCapturada = transacaoArgumentCaptor.getValue();

        // 3. Verificamos se cada campo da transação capturada foi preenchido corretamente
        assertNotNull(transacaoCapturada, "A transação não deveria ser nula.");
        assertEquals("SAQUE", transacaoCapturada.getTipo(), "O tipo da transação deveria ser SAQUE.");
        assertEquals(conta, transacaoCapturada.getContaRemetente(), "A conta remetente deveria ser a conta do saque.");
        assertNull(transacaoCapturada.getContaDestinatario(), "A conta destinatário deveria ser nula para saques.");
        assertEquals(valorSaque, transacaoCapturada.getValor(), "O valor deveria ser o valor do saque.");
        assertNotNull(transacaoCapturada.getData(), "A data da transação não deveria ser nula.");

        // 4. Verificamos se o método retornou o objeto que o repositório retornou
        assertEquals(transacaoSalva, resultado, "O retorno do método deve ser a transação salva pelo repositório.");
    }

    @Test
    @DisplayName("Deve mapear corretamente os dados para um TransacaoDTO de Saque")
    void deveMapearParaTransacaoDTOSaqueCorretamente() {
        // Cenário (Arrange)
        // 1. Criamos uma conta de origem com dados de exemplo
        Conta conta = new Conta();
        conta.setNumeroDaConta(1L);
        conta.setNome("Nome do Titular");

        // 2. Criamos uma transação de origem com dados de exemplo
        Transacao transacao = new Transacao();
        transacao.setTipo("SAQUE");
        transacao.setValor(200.0);
        transacao.setData(LocalDateTime.now());

        // Ação (Act)
        // Chamamos o método que queremos testar
        TransacaoDTO resultadoDTO = transacaoService.adicionarTransacaoDTOSaque(conta, transacao);

        // Verificação (Assert)
        // 3. Verificamos se o DTO resultante não é nulo e se seus campos foram preenchidos corretamente
        assertNotNull(resultadoDTO, "O TransacaoDTO não deveria ser nulo.");

        // Verificando os campos diretos do TransacaoDTO
        assertEquals("SAQUE", resultadoDTO.getTipo());
        assertEquals(200.0, resultadoDTO.getValor());
        assertEquals(transacao.getData(), resultadoDTO.getData());
        assertNull(resultadoDTO.getContaDestinatario(), "A conta destinatário no DTO de saque deve ser nula.");

        // Verificando os campos aninhados do ContaDTO do remetente
        assertNotNull(resultadoDTO.getContaRemetente(), "O ContaDTO do remetente não deveria ser nulo.");
        assertEquals(conta.getNumeroDaConta(), resultadoDTO.getContaRemetente().getNumeroDaConta());
        assertEquals("Nome do Titular", resultadoDTO.getContaRemetente().getNome());
    }

    @Test
    @DisplayName("Deve adicionar uma transação de DEPÓSITO com os dados corretos")
    void deveAdicionarTransacaoDeDepositoComSucesso() {
        // Cenário (Arrange)
        Conta contaDestino = new Conta();
        contaDestino.setNumeroDaConta(2L);
        double valorDeposito = 300.0;

        Transacao transacaoSalva = new Transacao();
        transacaoSalva.setId(2L); // ID simulado

        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacaoSalva);

        // Captor para o objeto Transacao
        ArgumentCaptor<Transacao> transacaoCaptor = ArgumentCaptor.forClass(Transacao.class);

        // Ação (Act)
        Transacao resultado = transacaoService.adicionarTransacaoDeposito(contaDestino, valorDeposito);

        // Verificação (Assert)
        // 1. Capturamos o argumento passado para o método save
        verify(transacaoRepository).save(transacaoCaptor.capture());

        // 2. Pegamos o objeto capturado
        Transacao transacaoCapturada = transacaoCaptor.getValue();

        // 3. Verificamos cada campo da transação capturada
        assertNotNull(transacaoCapturada, "A transação não deveria ser nula.");
        assertEquals("DEPOSITO", transacaoCapturada.getTipo(), "O tipo da transação deveria ser DEPOSITO.");
        assertNull(transacaoCapturada.getContaRemetente(), "A conta remetente deveria ser nula para depósitos.");
        assertEquals(contaDestino, transacaoCapturada.getContaDestinatario(), "A conta destinatário deveria ser a conta do depósito.");
        assertEquals(valorDeposito, transacaoCapturada.getValor(), "O valor deveria ser o valor do depósito.");
        assertNotNull(transacaoCapturada.getData(), "A data da transação não deveria ser nula.");

        // 4. Verificamos o retorno do método
        assertEquals(transacaoSalva, resultado, "O retorno do método deve ser a transação salva pelo repositório.");
    }

    @Test
    @DisplayName("Deve mapear corretamente os dados para um TransacaoDTO de Depósito")
    void deveMapearParaTransacaoDTODepositoCorretamente() {
        // Cenário (Arrange)
        // 1. Criamos a conta de destino que recebeu o depósito
        Conta conta = new Conta();
        conta.setNumeroDaConta(1L);
        conta.setNome("Nome do Favorecido");

        // 2. Criamos a transação de depósito
        Transacao transacao = new Transacao();
        transacao.setTipo("DEPOSITO");
        transacao.setValor(500.0);
        transacao.setData(LocalDateTime.now());

        // Ação (Act)
        // Chamamos o método que queremos testar
        TransacaoDTO resultadoDTO = transacaoService.adicionarTransacaoDTODeposito(conta, transacao);

        // Verificação (Assert)
        // 3. Verificamos se o DTO resultante foi preenchido corretamente
        assertNotNull(resultadoDTO, "O TransacaoDTO não deveria ser nulo.");

        // Verificando os campos diretos do TransacaoDTO
        assertEquals("DEPOSITO", resultadoDTO.getTipo());
        assertEquals(500.0, resultadoDTO.getValor());
        assertEquals(transacao.getData(), resultadoDTO.getData());
        assertNull(resultadoDTO.getContaRemetente(), "A conta remetente no DTO de depósito deve ser nula.");

        // Verificando os campos aninhados do ContaDTO do destinatário
        assertNotNull(resultadoDTO.getContaDestinatario(), "O ContaDTO do destinatário не должен быть nulo.");
        assertEquals(conta.getNumeroDaConta(), resultadoDTO.getContaDestinatario().getNumeroDaConta());
        assertEquals("Nome do Favorecido", resultadoDTO.getContaDestinatario().getNome());
    }

    @Test
    @DisplayName("Deve adicionar uma transação de TRANSFERÊNCIA com os dados corretos")
    void deveAdicionarTransacaoDeTransferenciaComSucesso() {
        // Cenário (Arrange)
        Conta contaRemetente = new Conta();
        contaRemetente.setNumeroDaConta(1L);

        Conta contaDestinatario = new Conta();
        contaDestinatario.setNumeroDaConta(2L);

        double valorTransferencia = 1000.0;

        Transacao transacaoSalva = new Transacao();
        transacaoSalva.setId(3L); // ID simulado

        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacaoSalva);

        ArgumentCaptor<Transacao> transacaoCaptor = ArgumentCaptor.forClass(Transacao.class);

        // Ação (Act)
        Transacao resultado = transacaoService.adicionarTransacaoTransferencia(contaRemetente, contaDestinatario, valorTransferencia);

        // Verificação (Assert)
        // 1. Capturamos o argumento passado para o método save
        verify(transacaoRepository).save(transacaoCaptor.capture());

        // 2. Pegamos o objeto capturado
        Transacao transacaoCapturada = transacaoCaptor.getValue();

        // 3. Verificamos cada campo da transação capturada
        assertNotNull(transacaoCapturada, "A transação não deveria ser nula.");
        assertEquals("TRANSFERENCIA", transacaoCapturada.getTipo(), "O tipo da transação deveria ser TRANSFERENCIA.");
        assertEquals(contaRemetente, transacaoCapturada.getContaRemetente(), "A conta remetente deveria ser a correta.");
        assertEquals(contaDestinatario, transacaoCapturada.getContaDestinatario(), "A conta destinatário deveria ser a correta.");
        assertEquals(valorTransferencia, transacaoCapturada.getValor(), "O valor deveria ser o valor da transferência.");
        assertNotNull(transacaoCapturada.getData(), "A data da transação não deveria ser nula.");

        // 4. Verificamos o retorno do método
        assertEquals(transacaoSalva, resultado, "O retorno do método deve ser a transação salva pelo repositório.");
    }

    @Test
    @DisplayName("Deve mapear corretamente os dados para um TransacaoDTO de Transferência")
    void deveMapearParaTransacaoDTOTransferenciaCorretamente() {
        // Cenário (Arrange)
        // 1. Criamos a conta do remetente
        Conta contaRemetente = new Conta();
        contaRemetente.setNumeroDaConta(1L);
        contaRemetente.setNome("Nome Remetente");

        // 2. Criamos a conta do destinatário
        Conta contaDestinatario = new Conta();
        contaDestinatario.setNumeroDaConta(2L);
        contaDestinatario.setNome("Nome Destinatario");

        // 3. Criamos a transação
        Transacao transacao = new Transacao();
        transacao.setTipo("TRANSFERENCIA");
        transacao.setValor(150.0);
        transacao.setData(LocalDateTime.now());

        // Ação (Act)
        TransacaoDTO resultadoDTO = transacaoService.adicionarTransacaoDTOTransferencia(contaRemetente, contaDestinatario, transacao);

        // Verificação (Assert)
        // 4. Verificamos se o DTO resultante foi preenchido com todos os dados
        assertNotNull(resultadoDTO, "O TransacaoDTO не должен быть nulo.");

        // Verificando os dados da transação
        assertEquals("TRANSFERENCIA", resultadoDTO.getTipo());
        assertEquals(150.0, resultadoDTO.getValor());
        assertEquals(transacao.getData(), resultadoDTO.getData());

        // Verificando os dados do remetente
        assertNotNull(resultadoDTO.getContaRemetente(), "O ContaDTO do remetente não deveria ser nulo.");
        assertEquals(contaRemetente.getNumeroDaConta(), resultadoDTO.getContaRemetente().getNumeroDaConta());
        assertEquals("Nome Remetente", resultadoDTO.getContaRemetente().getNome());

        // Verificando os dados do destinatário
        assertNotNull(resultadoDTO.getContaDestinatario(), "O ContaDTO do destinatário não deveria ser nulo.");
        assertEquals(contaDestinatario.getNumeroDaConta(), resultadoDTO.getContaDestinatario().getNumeroDaConta());
        assertEquals("Nome Destinatario", resultadoDTO.getContaDestinatario().getNome());
    }
}
