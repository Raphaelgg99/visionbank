package com.simuladorbanco.BancoDigital.controller;

import com.simuladorbanco.BancoDigital.config.JWTCreator;
import com.simuladorbanco.BancoDigital.config.JWTObject;
import com.simuladorbanco.BancoDigital.config.SecurityConfig;
import com.simuladorbanco.BancoDigital.dtos.Login;
import com.simuladorbanco.BancoDigital.dtos.Sessao;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import com.simuladorbanco.BancoDigital.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "1. Autenticação", description = "Rotas para login e geração de token de acesso")
public class LoginController {

    @Autowired
    LoginService loginService;

    @PostMapping("/login")
    @Operation(summary = "Autentica o usuário", description = "Recebe as credenciais (e-mail e senha), valida no sistema e retorna uma Sessão contendo o Token JWT. Este token deve ser usado no botão 'Authorize' para acessar as demais rotas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso. Token gerado."),
            @ApiResponse(responseCode = "403", description = "Credenciais inválidas (E-mail ou senha incorretos)", content = @Content),
            @ApiResponse(responseCode = "400", description = "Formato de requisição inválido", content = @Content)
    })
    public ResponseEntity<Sessao> logar(@RequestBody Login login){
        return ResponseEntity.ok(loginService.logar(login));
    }
}
