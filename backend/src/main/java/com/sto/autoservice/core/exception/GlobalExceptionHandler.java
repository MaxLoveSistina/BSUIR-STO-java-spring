package com.sto.autoservice.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ErrorResponse body(HttpStatus status, String reason, String message) {
        return new ErrorResponse(OffsetDateTime.now(ZoneOffset.UTC), status.value(), reason, message);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        StringBuilder message = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            message.append(field).append(": ").append(error.getDefaultMessage()).append("; ");
        });
        log.warn("Validation failed: {}", message);
        return body(HttpStatus.BAD_REQUEST, "Bad Request", message.toString().trim());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            BadRequestException.class,
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ErrorResponse handleBadRequest(Exception ex) {
        log.warn("Bad Request: {}", ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, "Bad Request", "Некорректный формат JSON или тела запроса");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public ErrorResponse handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return body(HttpStatus.UNAUTHORIZED, "Unauthorized", "Неверное имя пользователя или пароль");
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({LockedException.class, DisabledException.class})
    public ErrorResponse handleAccountState(AuthenticationException ex) {
        log.warn("Account unavailable: {}", ex.getMessage());
        return body(HttpStatus.FORBIDDEN, "Forbidden", "Аккаунт заблокирован или удалён");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ErrorResponse handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return body(HttpStatus.UNAUTHORIZED, "Unauthorized", "Неверные учетные данные или невалидный токен");
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ErrorResponse handleConflict(UserAlreadyExistsException ex) {
        log.warn("User conflict: {}", ex.getMessage());
        return body(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorResponse handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return body(HttpStatus.FORBIDDEN, "Forbidden", "Недостаточно прав для выполнения операции");
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleInternal(Exception ex) {
        log.error("Unhandled Internal Server Error", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Произошла внутренняя ошибка сервера");
    }
}
