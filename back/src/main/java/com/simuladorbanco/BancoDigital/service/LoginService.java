package com.simuladorbanco.BancoDigital.service;

import com.simuladorbanco.BancoDigital.config.JWTCreator;
import com.simuladorbanco.BancoDigital.config.JWTObject;
import com.simuladorbanco.BancoDigital.config.SecurityConfig;
import com.simuladorbanco.BancoDigital.dtos.Login;
import com.simuladorbanco.BancoDigital.dtos.Sessao;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;

@Service
public class LoginService {

    @Autowired
    ContaRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    public Sessao logar(Login login){
        Conta conta = repository.findByEmail(login.getEmail());
        if(conta!=null) {
            boolean passwordOk =  encoder.matches(login.getSenha(), conta.getSenha());
            if (!passwordOk) {
                throw new RuntimeException("Senha inválida para o login: " + login.getEmail());
            }

            System.out.println("Roles associadas ao usuário: " + conta.getRoles());
            Sessao sessao = new Sessao();
            sessao.setLogin(conta.getEmail());
            sessao.setId(conta.getNumeroDaConta());

            JWTObject jwtObject = new JWTObject();
            jwtObject.setSubject(conta.getEmail());
            jwtObject.setIssuedAt(new Date(System.currentTimeMillis()));
            jwtObject.setExpiration((new Date(System.currentTimeMillis() + SecurityConfig.EXPIRATION)));
            jwtObject.setRoles(conta.getRoles());
            sessao.setToken(JWTCreator.create(SecurityConfig.PREFIX, SecurityConfig.KEY, jwtObject));
            return sessao;
        }else {
            throw new RuntimeException("Erro ao tentar fazer login");
        }
    }
}
