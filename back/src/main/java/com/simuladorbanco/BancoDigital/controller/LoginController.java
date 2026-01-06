package com.simuladorbanco.BancoDigital.controller;

import com.simuladorbanco.BancoDigital.config.JWTCreator;
import com.simuladorbanco.BancoDigital.config.JWTObject;
import com.simuladorbanco.BancoDigital.config.SecurityConfig;
import com.simuladorbanco.BancoDigital.dtos.Login;
import com.simuladorbanco.BancoDigital.dtos.Sessao;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import com.simuladorbanco.BancoDigital.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<Sessao> logar(@RequestBody Login login){
        return ResponseEntity.ok(loginService.logar(login));
    }
}
