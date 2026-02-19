package com.simuladorbanco.BancoDigital.service;

import com.simuladorbanco.BancoDigital.dtos.TransacaoDTO;
import com.simuladorbanco.BancoDigital.dtos.TransferenciaRequest;
import com.simuladorbanco.BancoDigital.exception.*;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.model.Transacao;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import com.simuladorbanco.BancoDigital.repository.TransacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private TransacaoService transacaoService;


    public Conta criarConta(Conta conta){
        if (conta.getEmail() == null) {
            throw new EmailNullException();
        }
        if (conta.getSenha() == null) {
            throw new SenhaNullException();
        }
        Conta contaExistente = contaRepository.findByEmail(conta.getEmail());
        if (contaExistente!=null) {
            throw new EmailRepetidoException();
        }
        String senhaCriptografada = encoder.encode(conta.getSenha());
        conta.setSenha(senhaCriptografada);
        conta.setRoles(Collections.singletonList("USER"));
        return contaRepository.save(conta);
    }

    public Conta atualizarConta(Long numeroDaConta, Conta contaAtualizada){
        Conta conta = contaRepository.findById(numeroDaConta).
                orElseThrow(() -> new RuntimeException("Conta não encontrado"));
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!conta.getEmail().equals(usuarioLogado)) {
            throw new AccessDeniedException("Operação negada: Você não tem permissão para atualizar esta conta.");
        }
        conta.setNome(contaAtualizada.getNome());
        String senhaCriptografada = encoder.encode(contaAtualizada.getSenha());
        if(contaRepository.existsByEmail(contaAtualizada.getEmail()) &&
                !conta.getEmail().equals(contaAtualizada.getEmail())){
            throw new EmailRepetidoException();}
        conta.setEmail(contaAtualizada.getEmail());
        conta.setSenha(senhaCriptografada);
        conta.setSaldo(contaAtualizada.getSaldo());
        return contaRepository.save(conta);
    }

    public TransacaoDTO depositar(Double valor, Long numeroDaConta) {
        Conta conta = contaRepository.findById(numeroDaConta)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!conta.getEmail().equals(usuarioLogado)) {
            throw new AccessDeniedException("Operação negada: Você não tem permissão para depositar nessa conta.");
        }
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser maior que zero.");
        }
        conta.setSaldo(conta.getSaldo() + valor);
        System.out.println("Conta atualizada: " + conta);
        contaRepository.save(conta);
        Transacao transacao = transacaoService.adicionarTransacaoDeposito(conta, valor);
        TransacaoDTO transacaoDTO = transacaoService.adicionarTransacaoDTODeposito(conta, transacao);
        return transacaoDTO;
    }

    public TransacaoDTO sacar(Double valor, Long numeroDaConta){
        Conta conta = contaRepository.findById(numeroDaConta)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!conta.getEmail().equals(usuarioLogado)) {
            throw new AccessDeniedException("Operação negada: Você não tem permissão para sacar nessa conta.");
        }
        if(conta.getSaldo()<valor){
            throw new SaldoInsuficienteException();
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor do saque deve ser maior que zero.");
        }
        conta.setSaldo(conta.getSaldo() - valor);
        Transacao transacao = transacaoService.adicionarTransacaoSaque(conta,valor);
        TransacaoDTO transacaoDTO = transacaoService.adicionarTransacaoDTOSaque(conta,transacao);
        contaRepository.save(conta);
        return transacaoDTO;
    }

    public TransacaoDTO transferencia(TransferenciaRequest transferencia, Long numeroContaRemetente
    ){
        Conta contaRemetente = contaRepository.findById(numeroContaRemetente)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        Conta contaDestinario = contaRepository.findById(transferencia.getNumeroContaDestinatario())
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!contaRemetente.getEmail().equals(usuarioLogado)) {
            throw new AccessDeniedException("Operação negada: Você não tem permissão para realizar transações nessa conta.");
        }
        if(contaRemetente.getSaldo()<transferencia.getValor()){
            throw new SaldoInsuficienteException();
        }
        contaRemetente.setSaldo(contaRemetente.getSaldo() - transferencia.getValor());
        contaDestinario.setSaldo(contaDestinario.getSaldo() + transferencia.getValor());
        contaRepository.save(contaRemetente);
        contaRepository.save(contaDestinario);
        Transacao transacao = transacaoService.adicionarTransacaoTransferencia(contaRemetente, contaDestinario
                , transferencia.getValor());
        TransacaoDTO transacaoDTO = transacaoService.adicionarTransacaoDTOTransferencia(contaRemetente,
                contaDestinario,
                transacao);
        return transacaoDTO;
    }

    public void removerConta(Long numeroDaConta){
        Conta conta = contaRepository.findById(numeroDaConta).
                orElseThrow(() -> new RuntimeException("Conta não encontrado"));
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Se o e-mail da conta alvo for diferente do e-mail do Token, barra na hora!
        if (!conta.getEmail().equals(usuarioLogado)) {
            throw new AccessDeniedException("Operação negada: Você não tem permissão para deletar esta conta.");
        }
        List<Transacao> transacoesRemetente = transacaoRepository.findByContaRemetente(conta);
        Conta contaDefault = contaRepository.findById(99L).
                orElseThrow(() -> new RuntimeException("Conta não encontrado"));;
        for(Transacao transacao : transacoesRemetente){
            transacao.setContaRemetente(contaDefault);
        }
        List<Transacao> transacoesDestinatario = transacaoRepository.findByContaDestinatario(conta);
        for(Transacao transacao : transacoesDestinatario){
            transacao.setContaDestinatario(contaDefault);
        }
        contaRepository.delete(conta);
    }
    public List<Conta> listarTodos(){
        return contaRepository.findAll();
    }


    public Conta buscarConta( Long id) {
        Conta conta = contaRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Conta não encontrado"));
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!conta.getEmail().equals(usuarioLogado)) {
            throw new AccessDeniedException("Operação negada: Você não tem permissão para buscar esta conta.");
        }
        return conta;
    }
}

