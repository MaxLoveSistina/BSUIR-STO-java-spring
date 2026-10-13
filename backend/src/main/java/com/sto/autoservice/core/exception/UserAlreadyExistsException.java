package com.sto.autoservice.core.exception;

/**
 * Пользователь с таким username/email уже существует (409).
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
