package com.simuladorbanco.BancoDigital.service;

import com.simuladorbanco.BancoDigital.dtos.ContaDTO;
import com.simuladorbanco.BancoDigital.dtos.TransacaoDTO;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.model.Transacao;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import com.simuladorbanco.BancoDigital.repository.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoricoService {

    @Autowired
    ContaRepository contaRepository;

    @Autowired
    TransacaoRepository transacaoRepository;

    public List<Transacao> listarTransacoesDeposito(Long numeroDaConta){
        Conta conta = contaRepository.findById(numeroDaConta)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        return transacaoRepository.findByContaDestinatarioAndTipo(conta, "DEPOSITO");
    }

    public List<TransacaoDTO> listarTransacoesDTODeposito(List<Transacao> todasTransacoes){
        List<TransacaoDTO> transacoesDTO = todasTransacoes.stream()
                .map(transacao -> {
                    ContaDTO contaDTO = new ContaDTO();
                    contaDTO.setNumeroDaConta(transacao.getContaDestinatario().getNumeroDaConta());
                    contaDTO.setNome(transacao.getContaDestinatario().getNome());

                    TransacaoDTO transacaoDTO = new TransacaoDTO();
                    transacaoDTO.setTipo(transacao.getTipo());
                    transacaoDTO.setContaRemetente(null);
                    transacaoDTO.setContaDestinatario(contaDTO);
                    transacaoDTO.setValor(transacao.getValor());
                    transacaoDTO.setData(transacao.getData());

                    return transacaoDTO;
                })
                .collect(Collectors.toList());

        return transacoesDTO;
    }

    public List<Transacao> listarTransacoesSaque(Long numeroDaConta){
        Conta conta = contaRepository.findById(numeroDaConta)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        return transacaoRepository.findByContaRemetenteAndTipo(conta, "SAQUE");
    }

    public List<TransacaoDTO> listarTransacoesDTOSaque(List<Transacao> todasTransacoesSaque){
        List<TransacaoDTO> transacoesDTO = todasTransacoesSaque.stream()
                .map(transacao -> {
                    ContaDTO contaDTO = new ContaDTO();
                    contaDTO.setNumeroDaConta(transacao.getContaRemetente().getNumeroDaConta());
                    contaDTO.setNome(transacao.getContaRemetente().getNome());

                    TransacaoDTO transacaoDTO = new TransacaoDTO();
                    transacaoDTO.setTipo(transacao.getTipo());
                    transacaoDTO.setContaRemetente(contaDTO);
                    transacaoDTO.setContaDestinatario(null);
                    transacaoDTO.setValor(transacao.getValor());
                    transacaoDTO.setData(transacao.getData());

                    return transacaoDTO;
                })
                .collect(Collectors.toList());

        return transacoesDTO;
    }

    public List<Transacao> listarTransacoesTransferencia(Long numeroDaConta){
        Conta conta = contaRepository.findById(numeroDaConta)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        List<Transacao> todasTransacoesTransferencia = new ArrayList<>();
        todasTransacoesTransferencia.addAll(transacaoRepository.findByContaDestinatario(conta));
        todasTransacoesTransferencia.addAll(transacaoRepository.findByContaRemetente(conta));
        return todasTransacoesTransferencia;
    }

    public List<TransacaoDTO> listarTransacoesDTOTransferencia(List<Transacao> todasTransacoesTransferencia){
        return todasTransacoesTransferencia.stream()
            .filter(t -> t.getTipo().equals("TRANSFERENCIA"))
                .map(t -> {
                    TransacaoDTO dto = new TransacaoDTO();
                    dto.setTipo(t.getTipo());
                    dto.setValor(t.getValor());
                    dto.setData(t.getData());

                    ContaDTO cr = new ContaDTO();
                    cr.setNumeroDaConta(t.getContaRemetente().getNumeroDaConta());
                    cr.setNome(t.getContaRemetente().getNome());
                    dto.setContaRemetente(cr);

                    ContaDTO cd = new ContaDTO();
                    cd.setNumeroDaConta(t.getContaDestinatario().getNumeroDaConta());
                    cd.setNome(t.getContaDestinatario().getNome());
                    dto.setContaDestinatario(cd);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<TransacaoDTO> listarTodasAsTransacoes(Long numeroDaConta){
        List<Transacao> transacoesDeposito = listarTransacoesDeposito(numeroDaConta);
        List<TransacaoDTO> transacoesDepositoDTO = listarTransacoesDTODeposito(transacoesDeposito);
        List<Transacao> transacoesSaque = listarTransacoesSaque(numeroDaConta);
        List<TransacaoDTO> transacoesSaqueDTO = listarTransacoesDTOSaque(transacoesSaque);
        List<Transacao> transacoesTransferencia = listarTransacoesTransferencia(numeroDaConta);
        List<TransacaoDTO> transacoesTransferenciaDTO = listarTransacoesDTOTransferencia(transacoesTransferencia);
        List<TransacaoDTO> todasAsTransacoes = new ArrayList<>();
        todasAsTransacoes.addAll(transacoesDepositoDTO);
        todasAsTransacoes.addAll(transacoesSaqueDTO);
        todasAsTransacoes.addAll(transacoesTransferenciaDTO);
        return todasAsTransacoes.stream()
                .sorted(Comparator.comparing(TransacaoDTO::getData).reversed()) // Ordena por data
                .collect(Collectors.toList());
    }
}
