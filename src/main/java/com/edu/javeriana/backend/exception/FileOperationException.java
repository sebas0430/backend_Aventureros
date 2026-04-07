package com.edu.javeriana.backend.exception;

// Esta la usamos cuando algo sale mal al intentar guardar o borrar 
// un archivo físico en el disco duro (el sistema de archivos).
public class FileOperationException extends RuntimeException {
    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
