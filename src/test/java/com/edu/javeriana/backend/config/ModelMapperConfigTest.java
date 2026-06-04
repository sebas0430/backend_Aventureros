package com.edu.javeriana.backend.config;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.*;

class ModelMapperConfigTest {

    @Test
    void modelMapper_retornaInstanciaValida() {
        ModelMapperConfig config = new ModelMapperConfig();
        ModelMapper mapper = config.modelMapper();
        assertNotNull(mapper);
    }
}
