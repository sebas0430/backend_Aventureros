package com.edu.javeriana.backend.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void constructorCompleto_setea_todos_los_campos() {
        LocalDateTime ahora = LocalDateTime.now();
        ErrorResponse response = new ErrorResponse(ahora, 404, "Not Found", "Recurso no encontrado", "/api/test");

        assertEquals(ahora, response.getTimestamp());
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("Recurso no encontrado", response.getMessage());
        assertEquals("/api/test", response.getPath());
    }

    @Test
    void constructorVacio_permiteSetters() {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(500);
        response.setError("Internal Server Error");
        response.setMessage("Error inesperado");

        assertEquals(500, response.getStatus());
        assertEquals("Internal Server Error", response.getError());
        assertEquals("Error inesperado", response.getMessage());
    }
}
