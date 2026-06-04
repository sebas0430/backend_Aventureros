package com.edu.javeriana.backend.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessRuleExceptionTest {

    @Test
    void constructor_guardaMensaje() {
        BusinessRuleException ex = new BusinessRuleException("Regla de negocio violada");
        assertEquals("Regla de negocio violada", ex.getMessage());
    }

    @Test
    void esRuntimeException() {
        BusinessRuleException ex = new BusinessRuleException("error");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
