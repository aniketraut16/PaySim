package com.pg.PaySim.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<String> handleDuplicateResourceException(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<String> handleInvalidInputException(InvalidInputException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(AuthenticationFaliedException.class)
    public ResponseEntity<String> handleAuthenticationFaliedException(AuthenticationFaliedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(ResourceExpiredException.class)
    public ResponseEntity<String> handleResourceExpiredException(ResourceExpiredException ex) {
        return ResponseEntity.status(HttpStatus.GONE).body(ex.getMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<String> handleAuthorizationException(AuthorizationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
    
}
