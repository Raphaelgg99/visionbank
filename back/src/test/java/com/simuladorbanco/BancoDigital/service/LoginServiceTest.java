package com.simuladorbanco.BancoDigital.service;

import com.simuladorbanco.BancoDigital.config.SecurityConfig;
import com.simuladorbanco.BancoDigital.dtos.Login;
import com.simuladorbanco.BancoDigital.dtos.Sessao;
import com.simuladorbanco.BancoDigital.model.Conta;
import com.simuladorbanco.BancoDigital.repository.ContaRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private ContaRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private LoginService loginService;

    // IMPORTANTE: Como fizemos no JWTFilterTest, precisamos inicializar
    // as configurações estáticas, pois este teste não carrega o Spring.
    @BeforeAll
    static void setupConfig() {
        SecurityConfig.PREFIX = "Bearer";
        SecurityConfig.KEY = "uma-chave-secreta-muito-longa-para-testes-de-seguranca";
        SecurityConfig.EXPIRATION = 3600000L; // 1 hora em milissegundos
    }

    @Test
    @DisplayName("Deve logar com sucesso e retornar um objeto Sessao com token")
    void deveLogarComSucessoERetornarSessao() {
        // Cenário (Arrange)
        Login login = new Login();
        login.setEmail("usuario@teste.com");
        login.setSenha("senha123");

        Conta contaDoBanco = new Conta();
        contaDoBanco.setNumeroDaConta(1L); // <--- ADICIONE ISSO
        contaDoBanco.setEmail("usuario@teste.com");
        contaDoBanco.setSenha("senhaCriptografada");
        contaDoBanco.setRoles(Arrays.asList("USER"));

        // Mockamos o comportamento do repositório e do encoder
        when(repository.findByEmail("usuario@teste.com")).thenReturn(contaDoBanco);
        when(encoder.matches("senha123", "senhaCriptografada")).thenReturn(true);

        // Ação (Act)
        Sessao sessao = loginService.logar(login);

        // Verificação (Assert)
        assertNotNull(sessao);
        assertEquals("usuario@teste.com", sessao.getLogin());
        assertNotNull(sessao.getToken());
        assertTrue(sessao.getToken().startsWith(SecurityConfig.PREFIX));
    }

    @Test
    @DisplayName("Deve lançar uma RuntimeException para senha inválida")
    void deveLancarExcecaoParaSenhaInvalida() {
        // Cenário (Arrange)
        Login login = new Login();
        login.setEmail("usuario@teste.com");
        login.setSenha("senhaErrada");

        Conta contaDoBanco = new Conta();
        contaDoBanco.setEmail("usuario@teste.com");
        contaDoBanco.setSenha("senhaCriptografada");

        when(repository.findByEmail("usuario@teste.com")).thenReturn(contaDoBanco);
        // Simulamos que a senha não bate
        when(encoder.matches("senhaErrada", "senhaCriptografada")).thenReturn(false);

        // Ação e Verificação (Act & Assert)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loginService.logar(login);
        });

        assertEquals("Senha inválida para o login: usuario@teste.com", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar uma RuntimeException para usuário não encontrado")
    void deveLancarExcecaoParaUsuarioNaoEncontrado() {
        // Cenário (Arrange)
        Login login = new Login();
        login.setEmail("naoexiste@teste.com");
        login.setSenha("qualquerSenha");

        // Simulamos que o repositório não encontrou a conta
        when(repository.findByEmail("naoexiste@teste.com")).thenReturn(null);

        // Ação e Verificação (Act & Assert)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loginService.logar(login);
        });

        assertEquals("Erro ao tentar fazer login", exception.getMessage());
    }
}