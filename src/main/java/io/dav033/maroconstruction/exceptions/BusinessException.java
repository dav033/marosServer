package io.dav033.maroconstruction.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción para errores de validación de negocio
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(message, "BUSINESS_ERROR", HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, Object... args) {
        super(message, "BUSINESS_ERROR", HttpStatus.BAD_REQUEST, args);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, "BUSINESS_ERROR", HttpStatus.BAD_REQUEST, cause);
    }

    public BusinessException(String message, Throwable cause, Object... args) {
        super(message, "BUSINESS_ERROR", HttpStatus.BAD_REQUEST, cause, args);
    }
}
