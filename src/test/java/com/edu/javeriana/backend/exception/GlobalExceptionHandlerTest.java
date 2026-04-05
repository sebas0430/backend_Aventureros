package com.edu.javeriana.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Not found", response.getBody().getMessage());
    }

    @Test
    void testBusinessRuleException() {
        BusinessRuleException ex = new BusinessRuleException("Error");
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessRuleException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Error", response.getBody().getMessage());
    }

    @Test
    void testMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "Invalid value");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Object> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        
        Map<?, ?> errorsMap = (Map<?, ?>) body.get("mensajes");
        assertNotNull(errorsMap);
        assertEquals("Invalid value", errorsMap.get("field"));
    }

    @Test
    void testGlobalException() {
        Exception ex = new Exception("Internal error");
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Internal error", response.getBody().getMessage());
    }
}
