package com.simuladorbanco.BancoDigital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simuladorbanco.BancoDigital.dtos.Login;
import com.simuladorbanco.BancoDigital.dtos.Sessao;
import com.simuladorbanco.BancoDigital.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LoginService loginService;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        // Configura o MockMvc para testar o LoginController em modo standalone
        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
    }

    @Test
    @DisplayName("Deve logar com sucesso e retornar um objeto Sessao com token JWT")
    void deveLogarComSucessoERetornarSessao() throws Exception {
        // Cenário (Arrange)
        // 1. O que o cliente vai enviar na requisição
        Login loginRequest = new Login();
        loginRequest.setEmail("usuario@teste.com");
        loginRequest.setSenha("senha123");

        // 2. O que esperamos que o service retorne
        Sessao sessaoMock = new Sessao();
        sessaoMock.setLogin("usuario@teste.com");
        sessaoMock.setToken("Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImV4cCI6MTYxNjY4MzYyMiwiaWF0IjoxNjE2NjgxODIyLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXX0.fake-token-string");

        // 3. Configuramos o mock do serviço
        when(loginService.logar(any(Login.class))).thenReturn(sessaoMock);

        // Ação (Act) e Verificação (Assert)
        mockMvc.perform(post("/login") // Faz um POST para /login
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk()) // Espera um status 200 OK
                // Verifica se o JSON de resposta contém os campos esperados
                .andExpect(jsonPath("$.login").value("usuario@teste.com"))
                .andExpect(jsonPath("$.token").exists()); // Verifica se o campo 'token' existe na resposta
    }
}
