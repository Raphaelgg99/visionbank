package com.simuladorbanco.BancoDigital.controller;


import com.simuladorbanco.BancoDigital.exception.EmailNullException;
import com.simuladorbanco.BancoDigital.exception.SaldoInsuficienteException;
import com.simuladorbanco.BancoDigital.exception.SenhaNullException;
import com.simuladorbanco.BancoDigital.exception.SenhaRepetidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ContaControllerAdvice {

    @ExceptionHandler(EmailNullException.class)
    public ResponseEntity<Object> handleEmailNullException() {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("message", "Email vazio" );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(SenhaNullException.class)
    public ResponseEntity<Object> handleSenhaNullException() {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("message", "Senha vazia" );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Object> handleSaldoInsuficienteException() {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("message", "Saldo insuficiente" );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(RuntimeException.class) // Troque por RuntimeException se preferir
    public ResponseEntity<String> handleContaNaoEncontrada(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}


