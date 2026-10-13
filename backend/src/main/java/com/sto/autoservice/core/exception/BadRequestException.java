package com.sto.autoservice.core.exception;

/**
 * Нарушено бизнес-правило или переданы некорректные данные (400).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
