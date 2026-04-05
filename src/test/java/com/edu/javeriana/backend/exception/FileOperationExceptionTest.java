package com.edu.javeriana.backend.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FileOperationExceptionTest {

    @Test
    void testConstructorWithMessage() {
        FileOperationException ex = new FileOperationException("Test Error");
        assertEquals("Test Error", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        Throwable cause = new RuntimeException("Underlying issue");
        FileOperationException ex = new FileOperationException("Test Error", cause);
        assertEquals("Test Error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
