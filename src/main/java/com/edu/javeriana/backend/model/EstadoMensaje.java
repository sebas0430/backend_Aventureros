package com.edu.javeriana.backend.model;

public enum EstadoMensaje {
    ENVIADO,             // Mensaje enviado al bus esperando ser procesado
    PROCESADO,           // El mensaje fue recibido correctamente por otro proceso
    ERROR_SIN_RECEPTOR   // Falló, no hay proceso escuchando o hubo un error
}
