package com.simuladorbanco.BancoDigital.model;

import com.simuladorbanco.BancoDigital.dtos.ContaDTO;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_transacao")
@Data
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipo;

    @ManyToOne
    @JoinColumn(name = "conta_remetente_id")  // Relacionamento com a Conta, não DTO
    private Conta contaRemetente;

    @ManyToOne
    @JoinColumn(name = "conta_destinatario_id")
    private Conta contaDestinatario;

    @Column(nullable = false)
    private double valor;

    @Column(nullable = false)
    private LocalDateTime data;
}