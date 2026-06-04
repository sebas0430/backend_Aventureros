package com.edu.javeriana.backend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

/**
 * Clase utilitaria para generar, validar y extraer datos de tokens JWT.
 * Usa una clave secreta configurable desde application.properties.
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret:AventurerosSuperSecretKeyQueDebeTenerAlMenos32Caracteres!}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:3600000}") // 1 hora por defecto
    private long jwtExpirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Genera un token JWT con los datos del usuario.
     */
    public String generateToken(Long userId, String correo, String rol) {
        return Jwts.builder()
                .setSubject(correo)
                .claim("id", userId)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el correo (subject) del token.
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Valida que el token no esté alterado ni expirado.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT no soportado: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token JWT mal formado: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Firma JWT inválida: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token JWT vacío: {}", e.getMessage());
        }
        return false;
    }
}
