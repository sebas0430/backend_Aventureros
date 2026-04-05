package com.edu.javeriana.backend.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
//moldeo de datos, para mapear objetos de un tipo a otro, por ejemplo de entidad a dto o viceversa, para evitar escribir codigo repetitivo de asignacion de campos.