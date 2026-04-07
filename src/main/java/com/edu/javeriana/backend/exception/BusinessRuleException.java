package com.edu.javeriana.backend.exception;

// Esta excepción la usamos cuando el usuario intenta hacer algo que va en contra
// de las reglas del negocio (ej. borrar una empresa que tiene procesos activos).
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
