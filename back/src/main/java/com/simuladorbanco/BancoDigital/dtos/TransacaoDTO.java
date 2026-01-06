package com.simuladorbanco.BancoDigital.dtos;

import com.simuladorbanco.BancoDigital.model.Conta;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransacaoDTO {

    private String tipo;

    private ContaDTO contaRemetente;

    private ContaDTO contaDestinatario;

    private double valor;

    private LocalDateTime data;
}
