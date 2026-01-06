package com.simuladorbanco.BancoDigital.config;

import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTFilterTest {

    // Mocks para os objetos que o filtro recebe
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    // Injeta os mocks no nosso filtro
    @InjectMocks
    private JWTFilter jwtFilter;

    @BeforeAll
    static void setupConfig() {
        // Inicializamos manualmente os valores que o Spring injetaria na aplicação real
        SecurityConfig.PREFIX = "Bearer";
        SecurityConfig.KEY = "uma-chave-secreta-muito-longa-para-testes-de-seguranca";
    }
    @BeforeEach
    @AfterEach
    void cleanUp() {
        // Limpa o contexto de segurança antes e depois de cada teste
        // para garantir que um teste не interfira no outro
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar o usuário e continuar a cadeia de filtros com um token válido")
    void deveAutenticarUsuarioComTokenValido() throws ServletException, IOException {
        // Cenário (Arrange)
        // 1. Criamos um JWTObject para gerar um token válido
        JWTObject jwtObject = new JWTObject();
        jwtObject.setSubject("testuser");
        jwtObject.setIssuedAt(new Date(System.currentTimeMillis()));
        jwtObject.setExpiration(new Date(System.currentTimeMillis() + 60_000)); // Válido por 1 min
        jwtObject.setRoles(Arrays.asList("USER"));

        // 2. Geramos o token
        String validToken = JWTCreator.create(SecurityConfig.PREFIX, SecurityConfig.KEY, jwtObject);

        // 3. Configuramos o mock do request para retornar nosso token
        when(request.getHeader(JWTCreator.HEADER_AUTHORIZATION)).thenReturn(validToken);

        // Ação (Act)
        // Executamos o filtro
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Verificação (Assert)
        // 1. Verifica se a autenticação foi colocada no contexto de segurança
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getName());

        // 2. Verifica se a cadeia de filtros continuou
        verify(filterChain, times(1)).doFilter(request, response);

        // 3. Garante que nenhuma resposta de erro foi enviada
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("Deve continuar a cadeia de filtros sem autenticar se não houver token")
    void deveContinuarSemAutenticarQuandoNaoHaToken() throws ServletException, IOException {
        // Cenário
        // Simulamos uma requisição sem o cabeçalho de autorização
        when(request.getHeader(JWTCreator.HEADER_AUTHORIZATION)).thenReturn(null);

        // Ação
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Verificação
        // 1. O contexto de segurança deve permanecer vazio
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // 2. A cadeia de filtros deve continuar (para permitir acesso a endpoints públicos)
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve barrar a requisição com 403 Forbidden para token com assinatura inválida")
    void deveBarrarRequisicaoComTokenInvalido() throws ServletException, IOException {
        // Cenário
        // Criamos um token com uma chave e o assinamos
        JWTObject jwtObject = new JWTObject();
        jwtObject.setSubject("hacker");
        jwtObject.setIssuedAt(new Date());
        jwtObject.setExpiration(new Date(System.currentTimeMillis() + 60_000));
        jwtObject.setRoles(List.of("USER"));
        String tokenComChaveCerta = JWTCreator.create(SecurityConfig.PREFIX, "chave-original", jwtObject);

        // Simulamos que o token recebido foi adulterado ou assinado com a chave errada
        // (Aqui estamos usando o token certo mas validando com a chave errada no SecurityConfig)
        when(request.getHeader(JWTCreator.HEADER_AUTHORIZATION)).thenReturn(tokenComChaveCerta);

        // Ação
        // Assumindo que seu SecurityConfig.KEY seja diferente de "chave-original"
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Verificação
        // 1. A requisição deve ser barrada com status Forbidden
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());

        // 2. A cadeia de filtros NÃO deve continuar
        verify(filterChain, never()).doFilter(request, response);

        // 3. O contexto de segurança deve estar vazio
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
