package com.edu.javeriana.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "AventurerosSuperSecretKeyQueDebeTenerAlMenos32Caracteres!");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000L);
        jwtUtils.init();
    }

    @Test
    void generateToken_retornaTokenNoNulo() {
        String token = jwtUtils.generateToken(1L, "usuario@test.com", "ADMINISTRADOR_EMPRESA");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void getUsernameFromToken_retornaCorreo() {
        String token = jwtUtils.generateToken(1L, "usuario@test.com", "ADMINISTRADOR_EMPRESA");
        assertEquals("usuario@test.com", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void validateToken_tokenValido_retornaTrue() {
        String token = jwtUtils.generateToken(1L, "usuario@test.com", "EDITOR");
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void validateToken_tokenMalFormado_retornaFalse() {
        assertFalse(jwtUtils.validateToken("token.invalido.aqui"));
    }

    @Test
    void validateToken_tokenVacio_retornaFalse() {
        assertFalse(jwtUtils.validateToken(""));
    }

    @Test
    void validateToken_tokenExpirado_retornaFalse() {
        JwtUtils utilsConExpiracionNegativa = new JwtUtils();
        ReflectionTestUtils.setField(utilsConExpiracionNegativa, "jwtSecret", "AventurerosSuperSecretKeyQueDebeTenerAlMenos32Caracteres!");
        ReflectionTestUtils.setField(utilsConExpiracionNegativa, "jwtExpirationMs", -1000L);
        utilsConExpiracionNegativa.init();

        String tokenExpirado = utilsConExpiracionNegativa.generateToken(1L, "usuario@test.com", "EDITOR");
        assertFalse(jwtUtils.validateToken(tokenExpirado));
    }
}
