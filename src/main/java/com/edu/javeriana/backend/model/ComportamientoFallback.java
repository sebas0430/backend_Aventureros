package com.edu.javeriana.backend.model;

public enum ComportamientoFallback {
    GUARDAR,      // Guarda y lo deja pendiente para un catch tardío
    REINTENTAR,   // Reintenta enviar
    ERROR         // Lanza error y aborta
}
