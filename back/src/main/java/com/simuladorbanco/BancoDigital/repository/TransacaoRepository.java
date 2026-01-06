package com.simuladorbanco.BancoDigital.repository;

import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    List<Transacao> findByContaRemetente(Conta contaRemetente);
    List<Transacao> findByContaDestinatario(Conta contaDestinatario);
    List<Transacao> findByContaRemetenteAndTipo(Conta contaRemetente, String tipo);
    List<Transacao> findByContaDestinatarioAndTipo(Conta contaDestinatario, String tipo);
}