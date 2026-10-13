package com.sto.autoservice.core.exception;

/**
 * Запрашиваемый ресурс не найден (404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
