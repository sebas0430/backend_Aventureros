package com.edu.javeriana.backend.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_guardaMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso no encontrado");
        assertEquals("Recurso no encontrado", ex.getMessage());
    }

    @Test
    void esRuntimeException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("error");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
