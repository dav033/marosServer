package io.dav033.maroconstruction.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción para recursos no encontrados
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message, Object... args) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, args);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, cause);
    }

    public ResourceNotFoundException(String message, Throwable cause, Object... args) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, cause, args);
    }
}
