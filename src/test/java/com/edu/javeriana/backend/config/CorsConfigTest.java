package com.edu.javeriana.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.filter.CorsFilter;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {

    @Test
    void corsFilter_retornaFiltroValido() {
        CorsConfig config = new CorsConfig();
        CorsFilter filter = config.corsFilter();
        assertNotNull(filter);
    }
}
