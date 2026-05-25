package com.edu.javeriana.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Obtener el header de Autorización
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Si no hay header o no empieza con Bearer, continuamos la cadena de filtros sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el token
        jwt = authHeader.substring(7);
        
        try {
            userEmail = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // Token inválido o malformado
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Si tenemos email y no hay nadie autenticado aún en el contexto
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Validar el token 
            if (jwtUtil.isTokenValid(jwt)) {
                // Como no estamos usando UserDetailsService para roles todavía,
                // creamos una autenticación básica con ArrayList vacío de autoridades
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        new ArrayList<>()
                );
                
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // Establecer la autenticación en el contexto de seguridad de Spring
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 4. Continuar la petición
        filterChain.doFilter(request, response);
    }
}
