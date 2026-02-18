package com.simuladorbanco.BancoDigital.service;

import com.simuladorbanco.BancoDigital.dtos.ContaDTO;
import com.simuladorbanco.BancoDigital.dtos.TransacaoDTO;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.model.Transacao;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import com.simuladorbanco.BancoDigital.repository.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TransacaoService {

    @Autowired
    TransacaoRepository transacaoRepository;

    @Autowired
    ContaRepository contaRepository;

    public Transacao adicionarTransacaoSaque(Conta conta, double valor){
        Transacao transacao =  new Transacao();
        transacao.setTipo("SAQUE");
        transacao.setContaRemetente(conta);
        transacao.setContaDestinatario(null);
        transacao.setValor(valor);
        transacao.setData(LocalDateTime.now());
        return transacaoRepository.save(transacao);
    }

    public TransacaoDTO adicionarTransacaoDTOSaque(Conta conta, Transacao transacao){
        TransacaoDTO transacaoDTO = new TransacaoDTO();
        ContaDTO contaDTO = new ContaDTO();
        contaDTO.setNumeroDaConta(conta.getNumeroDaConta());
        contaDTO.setNome(conta.getNome());
        transacaoDTO.setTipo(transacao.getTipo());
        transacaoDTO.setContaRemetente(contaDTO);
        transacaoDTO.setContaDestinatario(null);
        transacaoDTO.setValor(transacao.getValor());
        transacaoDTO.setData(transacao.getData());
        return transacaoDTO;
    }

    public Transacao adicionarTransacaoDeposito(Conta conta, double valor){
        Transacao transacao =  new Transacao();
        transacao.setTipo("DEPOSITO");
        transacao.setContaRemetente(null);
        transacao.setContaDestinatario(conta);
        transacao.setValor(valor);
        transacao.setData(LocalDateTime.now());
        return transacaoRepository.save(transacao);
    }

    public TransacaoDTO adicionarTransacaoDTODeposito(Conta conta, Transacao transacao){
        TransacaoDTO transacaoDTO = new TransacaoDTO();
        ContaDTO contaDTO = new ContaDTO();
        contaDTO.setNumeroDaConta(conta.getNumeroDaConta());
        contaDTO.setNome(conta.getNome());
        transacaoDTO.setTipo(transacao.getTipo());
        transacaoDTO.setContaRemetente(null);
        transacaoDTO.setContaDestinatario(contaDTO);
        transacaoDTO.setValor(transacao.getValor());
        transacaoDTO.setData(transacao.getData());
        return transacaoDTO;
    }

    public Transacao adicionarTransacaoTransferencia(Conta contaRemetente, Conta contaDestinario
            , double valor){
        Transacao transacao =  new Transacao();
        if (Objects.equals(contaRemetente.getNumeroDaConta(), contaDestinario.getNumeroDaConta())){
            throw new RuntimeException("Você não pode realizar uma transferencia para si mesmo");
        }
        transacao.setTipo("TRANSFERENCIA");
        transacao.setContaRemetente(contaRemetente);
        transacao.setContaDestinatario(contaDestinario);
        transacao.setValor(valor);
        transacao.setData(LocalDateTime.now());
        return transacaoRepository.save(transacao);
    }

    public TransacaoDTO adicionarTransacaoDTOTransferencia(Conta contaRemetente, Conta contaDestinatario,
                                                           Transacao transacao){
        TransacaoDTO transacaoDTO = new TransacaoDTO();
        ContaDTO contaDTORementente = new ContaDTO();
        contaDTORementente.setNumeroDaConta(contaRemetente.getNumeroDaConta());
        contaDTORementente.setNome(contaRemetente.getNome());
        ContaDTO contaDTODestinatario = new ContaDTO();
        contaDTODestinatario.setNumeroDaConta(contaDestinatario.getNumeroDaConta());
        contaDTODestinatario.setNome(contaDestinatario.getNome());
        transacaoDTO.setTipo(transacao.getTipo());
        transacaoDTO.setContaRemetente(contaDTORementente);
        transacaoDTO.setContaDestinatario(contaDTODestinatario);
        transacaoDTO.setValor(transacao.getValor());
        transacaoDTO.setData(transacao.getData());
        return transacaoDTO;
    }
}




