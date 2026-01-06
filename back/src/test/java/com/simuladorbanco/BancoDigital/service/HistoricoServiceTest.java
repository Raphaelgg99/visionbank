package com.simuladorbanco.BancoDigital.service;
import com.simuladorbanco.BancoDigital.dtos.TransacaoDTO;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.model.Transacao;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import com.simuladorbanco.BancoDigital.repository.TransacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricoServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Spy
    @InjectMocks
    private HistoricoService historicoService;

    @Test
    @DisplayName("Deve listar as transações de depósito de uma conta existente")
    void deveListarTransacoesDeDepositoComSucesso() {
        // Cenário (Arrange)
        Long numeroDaConta = 1L;
        Conta conta = new Conta();
        conta.setNumeroDaConta(numeroDaConta);

        // Simulamos uma lista de transações que seria retornada pelo repositório
        List<Transacao> transacoesEsperadas = Arrays.asList(new Transacao(), new Transacao());

        // Mockamos o comportamento dos repositórios
        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(conta));
        when(transacaoRepository.findByContaDestinatario(conta)).thenReturn(transacoesEsperadas);

        // Ação (Act)
        List<Transacao> resultado = historicoService.listarTransacoesDeposito(numeroDaConta);

        // Verificação (Assert)
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(transacoesEsperadas, resultado);

        // Verifica se os métodos corretos dos repositórios foram chamados
        verify(contaRepository).findById(numeroDaConta);
        verify(transacaoRepository).findByContaDestinatario(conta);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao listar depósitos de uma conta inexistente")
    void deveLancarExcecaoQuandoContaNaoEncontradaParaListarDepositos() {
        // Cenário
        Long numeroDaContaInexistente = 99L;
        // Mockamos o repositório para simular que a conta não foi encontrada
        when(contaRepository.findById(numeroDaContaInexistente)).thenReturn(Optional.empty());

        // Ação e Verificação
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historicoService.listarTransacoesDeposito(numeroDaContaInexistente);
        });

        assertEquals("Conta não encontrada", exception.getMessage());

        // Garante que o transacaoRepository nunca foi chamado, pois a execução parou antes
        verify(transacaoRepository, never()).findByContaDestinatario(any(Conta.class));
    }

    @Test
    @DisplayName("Deve mapear uma lista de transações de depósito para uma lista de DTOs")
    void deveMapearListaDeTransacoesParaListaDeDTOsCorretamente() {
        // Cenário (Arrange)
        // Criamos uma conta que será o destinatário em ambas as transações
        Conta contaDestino = new Conta();
        contaDestino.setNumeroDaConta(1L);
        contaDestino.setNome("Titular Recebedor");

        // Criamos a primeira transação de exemplo
        Transacao t1 = new Transacao();
        t1.setTipo("DEPOSITO");
        t1.setValor(100.0);
        t1.setData(LocalDateTime.now());
        t1.setContaDestinatario(contaDestino);

        // Criamos a segunda transação de exemplo
        Transacao t2 = new Transacao();
        t2.setTipo("DEPOSITO");
        t2.setValor(250.0);
        t2.setData(LocalDateTime.now().minusDays(1));
        t2.setContaDestinatario(contaDestino);

        // Colocamos as transações em uma lista de entrada
        List<Transacao> transacoesDeEntrada = Arrays.asList(t1, t2);

        // Ação (Act)
        List<TransacaoDTO> resultadoDTOs = historicoService.listarTransacoesDTODeposito(transacoesDeEntrada);

        // Verificação (Assert)
        assertNotNull(resultadoDTOs, "A lista de DTOs não deveria ser nula.");
        assertEquals(2, resultadoDTOs.size(), "A lista de DTOs deveria ter 2 elementos.");

        // Verificamos os dados do primeiro DTO
        TransacaoDTO dto1 = resultadoDTOs.get(0);
        assertEquals("DEPOSITO", dto1.getTipo());
        assertEquals(100.0, dto1.getValor());
        assertNull(dto1.getContaRemetente());
        assertNotNull(dto1.getContaDestinatario());
        assertEquals(contaDestino.getNumeroDaConta(), dto1.getContaDestinatario().getNumeroDaConta());
        assertEquals("Titular Recebedor", dto1.getContaDestinatario().getNome());
    }

    @Test
    @DisplayName("Deve retornar uma lista de DTOs vazia quando a lista de entrada for vazia")
    void deveRetornarListaVaziaQuandoEntradaForVazia() {
        // Cenário (Arrange)
        List<Transacao> transacoesVazias = Collections.emptyList();

        // Ação (Act)
        List<TransacaoDTO> resultadoDTOs = historicoService.listarTransacoesDTODeposito(transacoesVazias);

        // Verificação (Assert)
        assertNotNull(resultadoDTOs, "A lista de DTOs não deveria ser nula.");
        assertTrue(resultadoDTOs.isEmpty(), "A lista de DTOs deveria estar vazia.");
    }

    @Test
    @DisplayName("Deve listar as transações de saque de uma conta existente")
    void deveListarTransacoesDeSaqueComSucesso() {
        // Cenário (Arrange)
        Long numeroDaConta = 1L;
        Conta conta = new Conta();
        conta.setNumeroDaConta(numeroDaConta);

        // Simulamos uma lista de transações que seria retornada pelo repositório
        List<Transacao> transacoesEsperadas = Arrays.asList(new Transacao(), new Transacao());

        // Mockamos o comportamento dos repositórios
        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(conta));
        // A única mudança: chamamos findByContaRemetente em vez de findByContaDestinatario
        when(transacaoRepository.findByContaRemetente(conta)).thenReturn(transacoesEsperadas);

        // Ação (Act)
        // Supondo que o nome do método seja listarTransacoesSaque
        List<Transacao> resultado = historicoService.listarTransacoesSaque(numeroDaConta);

        // Verificação (Assert)
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(transacoesEsperadas, resultado);

        verify(contaRepository).findById(numeroDaConta);
        verify(transacaoRepository).findByContaRemetente(conta);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao listar saques de uma conta inexistente")
    void deveLancarExcecaoQuandoContaNaoEncontradaParaListarSaques() {
        // Cenário
        Long numeroDaContaInexistente = 99L;
        when(contaRepository.findById(numeroDaContaInexistente)).thenReturn(Optional.empty());

        // Ação e Verificação
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historicoService.listarTransacoesSaque(numeroDaContaInexistente);
        });

        assertEquals("Conta não encontrada", exception.getMessage());
        verify(transacaoRepository, never()).findByContaRemetente(any(Conta.class));
    }


// ========== TESTES PARA MAPEAMENTO DE SAQUES PARA DTOs ==========

    @Test
    @DisplayName("Deve mapear uma lista de transações de saque para uma lista de DTOs")
    void deveMapearListaDeTransacoesDeSaqueParaListaDeDTOs() {
        // Cenário (Arrange)
        Conta contaRemetente = new Conta();
        contaRemetente.setNumeroDaConta(1L);
        contaRemetente.setNome("Titular Sacador");

        Transacao t1 = new Transacao();
        t1.setTipo("SAQUE");
        t1.setValor(50.0);
        t1.setData(LocalDateTime.now());
        t1.setContaRemetente(contaRemetente);

        List<Transacao> transacoesDeEntrada = Arrays.asList(t1);

        // Ação (Act)
        // Supondo que o nome do método seja listarTransacoesDTOSaque
        List<TransacaoDTO> resultadoDTOs = historicoService.listarTransacoesDTOSaque(transacoesDeEntrada);

        // Verificação (Assert)
        assertNotNull(resultadoDTOs);
        assertEquals(1, resultadoDTOs.size());

        TransacaoDTO dto1 = resultadoDTOs.get(0);
        assertEquals("SAQUE", dto1.getTipo());
        assertEquals(50.0, dto1.getValor());
        assertNull(dto1.getContaDestinatario());
        assertNotNull(dto1.getContaRemetente());
        assertEquals(contaRemetente.getNumeroDaConta(), dto1.getContaRemetente().getNumeroDaConta());
        assertEquals("Titular Sacador", dto1.getContaRemetente().getNome());
    }

    @Test
    @DisplayName("Deve retornar uma lista de DTOs de saque vazia quando a lista de entrada for vazia")
    void deveRetornarListaVaziaDeDTOSaqueQuandoEntradaForVazia() {
        // Cenário (Arrange)
        List<Transacao> transacoesVazias = Collections.emptyList();

        // Ação (Act)
        List<TransacaoDTO> resultadoDTOs = historicoService.listarTransacoesDTOSaque(transacoesVazias);

        // Verificação (Assert)
        assertNotNull(resultadoDTOs);
        assertTrue(resultadoDTOs.isEmpty());
    }

    @Test
    @DisplayName("Deve listar as transações de transferência (enviadas e recebidas) de uma conta")
    void deveListarTodasAsTransferenciasComSucesso() {
        // Cenário (Arrange)
        Long numeroDaConta = 1L;
        Conta conta = new Conta();
        conta.setNumeroDaConta(numeroDaConta);

        // Simulamos listas de transações enviadas e recebidas
        List<Transacao> transacoesEnviadas = Arrays.asList(new Transacao()); // 1 enviada
        List<Transacao> transacoesRecebidas = Arrays.asList(new Transacao(), new Transacao()); // 2 recebidas

        // Mockamos o comportamento dos repositórios
        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(conta));
        when(transacaoRepository.findByContaDestinatario(conta)).thenReturn(transacoesRecebidas);
        when(transacaoRepository.findByContaRemetente(conta)).thenReturn(transacoesEnviadas);

        // Ação (Act)
        List<Transacao> resultado = historicoService.listarTransacoesTransferencia(numeroDaConta);

        // Verificação (Assert)
        assertNotNull(resultado);
        // O tamanho total deve ser a soma das duas listas
        assertEquals(3, resultado.size());

        // Verificamos se a lista final contém todos os elementos das listas originais
        assertTrue(resultado.containsAll(transacoesEnviadas));
        assertTrue(resultado.containsAll(transacoesRecebidas));

        // Verificamos se os métodos corretos foram chamados
        verify(contaRepository).findById(numeroDaConta);
        verify(transacaoRepository).findByContaDestinatario(conta);
        verify(transacaoRepository).findByContaRemetente(conta);
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia de transferências se a conta não tiver nenhuma")
    void deveRetornarListaVaziaDeTransferenciasQuandoNaoHouver() {
        // Cenário (Arrange)
        Long numeroDaConta = 1L;
        Conta conta = new Conta();
        conta.setNumeroDaConta(numeroDaConta);

        // Simulamos que a conta não tem transações de transferência
        when(contaRepository.findById(numeroDaConta)).thenReturn(Optional.of(conta));
        when(transacaoRepository.findByContaDestinatario(conta)).thenReturn(Collections.emptyList());
        when(transacaoRepository.findByContaRemetente(conta)).thenReturn(Collections.emptyList());

        // Ação (Act)
        List<Transacao> resultado = historicoService.listarTransacoesTransferencia(numeroDaConta);

        // Verificação (Assert)
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao listar transferências de uma conta inexistente")
    void deveLancarExcecaoQuandoContaNaoEncontradaParaListarTransferencias() {
        // Cenário
        Long numeroDaContaInexistente = 99L;
        when(contaRepository.findById(numeroDaContaInexistente)).thenReturn(Optional.empty());

        // Ação e Verificação
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historicoService.listarTransacoesTransferencia(numeroDaContaInexistente);
        });

        assertEquals("Conta não encontrada", exception.getMessage());
        verify(transacaoRepository, never()).findByContaDestinatario(any(Conta.class));
        verify(transacaoRepository, never()).findByContaRemetente(any(Conta.class));
    }

    @Test
    @DisplayName("Deve mapear uma lista de transações de transferência para uma lista de DTOs")
    void deveMapearListaDeTransferenciasParaDTOsCorretamente() {
        // Cenário (Arrange)
        Conta remetente = new Conta();
        remetente.setNumeroDaConta(1L);
        remetente.setNome("Remetente Teste");

        Conta destinatario = new Conta();
        destinatario.setNumeroDaConta(2L);
        destinatario.setNome("Destinatario Teste");

        Transacao t1 = new Transacao();
        t1.setTipo("TRANSFERENCIA");
        t1.setValor(500.0);
        t1.setData(LocalDateTime.now());
        t1.setContaRemetente(remetente);
        t1.setContaDestinatario(destinatario);

        List<Transacao> transacoesDeEntrada = Arrays.asList(t1);

        // Ação (Act)
        List<TransacaoDTO> resultadoDTOs = historicoService.listarTransacoesDTOTransferencia(transacoesDeEntrada);

        // Verificação (Assert)
        assertNotNull(resultadoDTOs);
        assertEquals(1, resultadoDTOs.size());

        TransacaoDTO dto = resultadoDTOs.get(0);
        assertEquals("TRANSFERENCIA", dto.getTipo());
        assertEquals(500.0, dto.getValor());

        // Verifica dados do remetente
        assertNotNull(dto.getContaRemetente());
        assertEquals(remetente.getNumeroDaConta(), dto.getContaRemetente().getNumeroDaConta());
        assertEquals("Remetente Teste", dto.getContaRemetente().getNome());

        // Verifica dados do destinatário
        assertNotNull(dto.getContaDestinatario());
        assertEquals(destinatario.getNumeroDaConta(), dto.getContaDestinatario().getNumeroDaConta());
        assertEquals("Destinatario Teste", dto.getContaDestinatario().getNome());
    }

    @Test
    @DisplayName("Deve filtrar transações que não são do tipo TRANSFERENCIA ao mapear para DTO")
    void deveFiltrarTransacoesQueNaoSaoDeTransferencia() {
        // Cenário (Arrange)
        Conta c1 = new Conta();
        Conta c2 = new Conta();

        // Criamos uma lista mista de transações
        Transacao transferencia = new Transacao();
        transferencia.setTipo("TRANSFERENCIA");
        transferencia.setContaRemetente(c1); // Campos mínimos para não dar erro
        transferencia.setContaDestinatario(c2);

        Transacao saque = new Transacao();
        saque.setTipo("SAQUE");
        saque.setContaRemetente(c1);

        List<Transacao> transacoesMistas = Arrays.asList(transferencia, saque);

        // Ação (Act)
        List<TransacaoDTO> resultadoDTOs = historicoService.listarTransacoesDTOTransferencia(transacoesMistas);

        // Verificação (Assert)
        assertNotNull(resultadoDTOs);
        // A lista final deve ter apenas 1 elemento, pois o saque foi filtrado
        assertEquals(1, resultadoDTOs.size());
        assertEquals("TRANSFERENCIA", resultadoDTOs.get(0).getTipo());
    }

    @Test
    @DisplayName("Deve retornar uma lista de DTOs de transferência vazia quando a entrada for vazia")
    void deveRetornarListaVaziaDeDTOTransferenciaParaEntradaVazia() {
        // Cenário (Arrange)
        List<Transacao> transacoesVazias = Collections.emptyList();

        // Ação (Act)
        List<TransacaoDTO> resultadoDTOs = historicoService.listarTransacoesDTOTransferencia(transacoesVazias);

        // Verificação (Assert)
        assertNotNull(resultadoDTOs);
        assertTrue(resultadoDTOs.isEmpty());
    }

    @Test
    @DisplayName("Deve listar todas as transações e ordená-las por data decrescente")
    void deveListarTodasAsTransacoesEOrdenarPorDataDecrescente() {
        // Cenário (Arrange)
        Long numeroDaConta = 1L;

        // Criamos DTOs com datas diferentes para testar a ordenação
        TransacaoDTO dtoDeposito = new TransacaoDTO();
        dtoDeposito.setData(LocalDateTime.now().minusDays(2)); // Mais antigo

        TransacaoDTO dtoSaque = new TransacaoDTO();
        dtoSaque.setData(LocalDateTime.now().minusDays(1)); // Intermediário

        TransacaoDTO dtoTransferencia = new TransacaoDTO();
        dtoTransferencia.setData(LocalDateTime.now()); // Mais recente

        // Usamos o Spy para interceptar as chamadas aos outros métodos e retornar nossas listas de DTOs
        // A sintaxe doReturn(...).when(spy).metodo() é a mais segura para usar com Spies.
        // Não precisamos testar a lógica desses métodos de novo, apenas o resultado deles.
        doReturn(List.of(dtoDeposito)).when(historicoService).listarTransacoesDTODeposito(anyList());
        doReturn(List.of(dtoSaque)).when(historicoService).listarTransacoesDTOSaque(anyList());
        doReturn(List.of(dtoTransferencia)).when(historicoService).listarTransacoesDTOTransferencia(anyList());

        // Também podemos "calar" os métodos que buscam as entidades para não depender dos repositórios neste teste
        doReturn(Collections.emptyList()).when(historicoService).listarTransacoesDeposito(numeroDaConta);
        doReturn(Collections.emptyList()).when(historicoService).listarTransacoesSaque(numeroDaConta);
        doReturn(Collections.emptyList()).when(historicoService).listarTransacoesTransferencia(numeroDaConta);

        // Ação (Act)
        List<TransacaoDTO> resultado = historicoService.listarTodasAsTransacoes(numeroDaConta);

        // Verificação (Assert)
        assertNotNull(resultado);
        assertEquals(3, resultado.size(), "A lista combinada deveria ter 3 transações.");

        // Verificamos a ORDEM da lista. A mais recente deve vir primeiro.
        assertEquals(dtoTransferencia, resultado.get(0), "A primeira transação deveria ser a mais recente (transferência).");
        assertEquals(dtoSaque, resultado.get(1), "A segunda transação deveria ser a intermediária (saque).");
        assertEquals(dtoDeposito, resultado.get(2), "A terceira transação deveria ser a mais antiga (depósito).");
    }



}
