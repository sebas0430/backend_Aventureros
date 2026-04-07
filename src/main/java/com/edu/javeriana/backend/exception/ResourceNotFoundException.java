package com.edu.javeriana.backend.exception;

// Esta es la clásica excepción para cuando buscamos algo (un usuario, un proceso)
// y no aparece por ningún lado en la base de datos.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
