package com.edu.javeriana.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Generamos una llave segura para firmar los tokens (HS256 requiere mínimo 256 bits).
    // Si tuvieras una propiedad en application.properties, podrías inyectarla. Aquí usamos una constante quemada 
    // como base, o generamos una en memoria si no te importa que los tokens expiren al reiniciar el servidor.
    // Usaremos una llave fuerte para evitar errores en JJWT.
    private static final String SECRET = "AventurerosSuperSecretKeyQueDebeSerMuyLargaParaHS2561234567890!";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());

    // El token durará 2 horas por defecto.
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, String userDetailsUsername) {
        final String username = extractUsername(token);
        return (username.equals(userDetailsUsername) && !isTokenExpired(token));
    }
    
    // Método auxiliar si solo validamos vigencia (ej. en el refresh)
    public Boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
