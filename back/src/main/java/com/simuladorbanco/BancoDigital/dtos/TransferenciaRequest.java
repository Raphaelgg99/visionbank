package com.simuladorbanco.BancoDigital.dtos;

public class TransferenciaRequest {
    private double valor;
    private Long numeroContaDestinatario;

    // Construtores
    public TransferenciaRequest() {}

    /*public TransferenciaRequest(double valor, Long numeroContaDestinatario) {
        this.valor = valor;
        this.numeroContaDestinatario = numeroContaDestinatario;
    }*/

    // Getters e setters
    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public Long getNumeroContaDestinatario() {
        return numeroContaDestinatario;
    }

    public void setNumeroContaDestinatario(Long numeroContaDestinatario) {
        this.numeroContaDestinatario = numeroContaDestinatario;
    }
}