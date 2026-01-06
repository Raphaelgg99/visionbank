package com.simuladorbanco.BancoDigital.config;

import com.simuladorbanco.BancoDigital.config.JWTObject;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class JWTCreatorTest {

    // Chave secreta para os testes. Em um app real, ela viria de um arquivo de configuração.
    private static final String SECRET_KEY = "uma-chave-secreta-muito-longa-e-segura-para-testes";
    private static final String PREFIX = "Bearer";

    private JWTObject jwtObject;

    @BeforeEach
    void setUp() {
        // Cria um objeto JWT base para ser usado nos testes
        jwtObject = new JWTObject();
        jwtObject.setSubject("testuser@email.com");
        jwtObject.setIssuedAt(new Date());
        // Define a expiração para 1 minuto no futuro
        jwtObject.setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)));
        jwtObject.setRoles(Arrays.asList("USER", "ROLE_ADMIN")); // Um com prefixo, outro sem
    }

    @Test
    @DisplayName("Deve criar um token, validá-lo e os dados devem ser consistentes (ida e volta)")
    void deveCriarEValidarTokenComSucesso() {
        // --- Ação (Act) ---
        // 1. Cria o token
        String token = JWTCreator.create(PREFIX, SECRET_KEY, jwtObject);
        System.out.println("Token Gerado: " + token);

        // 2. Valida o token e extrai o objeto
        JWTObject parsedObject = JWTCreator.create(token, PREFIX, SECRET_KEY);

        // --- Verificação (Assert) ---
        assertNotNull(parsedObject);
        assertEquals(jwtObject.getSubject(), parsedObject.getSubject());

        // Compara as datas com uma margem de tolerância para milissegundos
        assertEquals(TimeUnit.MILLISECONDS.toSeconds(jwtObject.getIssuedAt().getTime()),
                TimeUnit.MILLISECONDS.toSeconds(parsedObject.getIssuedAt().getTime()));

        // Verifica se a lógica de adicionar "ROLE_" funcionou
        List<String> roles = parsedObject.getRoles();
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_USER"), "Deveria ter adicionado o prefixo a 'USER'");
        assertTrue(roles.contains("ROLE_ADMIN"), "Deveria ter mantido 'ROLE_ADMIN'");
    }

    @Test
    @DisplayName("Deve lançar SignatureException ao tentar validar token com chave errada")
    void deveLancarSignatureExceptionParaChaveInvalida() {
        // Cenário (Arrange)
        String token = JWTCreator.create(PREFIX, SECRET_KEY, jwtObject);
        String chaveInvalida = "outra-chave-totalmente-diferente";

        // Ação e Verificação
        assertThrows(SignatureException.class, () -> {
            JWTCreator.create(token, PREFIX, chaveInvalida);
        }, "Deveria lançar SignatureException para uma chave inválida.");
    }

    @Test
    @DisplayName("Deve lançar ExpiredJwtException ao tentar validar token expirado")
    void deveLancarExpiredJwtExceptionParaTokenExpirado() {
        // Cenário (Arrange)
        // Cria um token que expirou 1 segundo atrás
        jwtObject.setExpiration(new Date(System.currentTimeMillis() - 1000));
        String tokenExpirado = JWTCreator.create(PREFIX, SECRET_KEY, jwtObject);

        // Ação e Verificação
        assertThrows(ExpiredJwtException.class, () -> {
            JWTCreator.create(tokenExpirado, PREFIX, SECRET_KEY);
        }, "Deveria lançar ExpiredJwtException para um token expirado.");
    }

    @Test
    @DisplayName("Deve lançar MalformedJwtException para uma string de token inválida")
    void deveLancarMalformedJwtExceptionParaTokenInvalido() {
        // Cenário (Arrange)
        String tokenMalformado = "isto.nao.e.um.jwt";

        // Ação e Verificação
        assertThrows(MalformedJwtException.class, () -> {
            JWTCreator.create(tokenMalformado, PREFIX, SECRET_KEY);
        }, "Deveria lançar MalformedJwtException para um token malformado.");
    }
}
