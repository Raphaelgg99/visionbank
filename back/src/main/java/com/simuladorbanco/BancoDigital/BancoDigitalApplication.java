package com.simuladorbanco.BancoDigital;

import com.simuladorbanco.BancoDigital.controller.ContaController;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.simuladorbanco.BancoDigital.repository")
@EntityScan(basePackages = "com.simuladorbanco.BancoDigital.model")
public class BancoDigitalApplication {
    public static void main(String[] args) {
        SpringApplication.run(BancoDigitalApplication.class, args);
    }
}

