package io.dav033.maroconstruction.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción para errores de servicios externos
 */
public class ExternalServiceException extends BaseException {

    public ExternalServiceException(String message) {
        super(message, "EXTERNAL_SERVICE_ERROR", HttpStatus.BAD_GATEWAY);
    }

    public ExternalServiceException(String message, Object... args) {
        super(message, "EXTERNAL_SERVICE_ERROR", HttpStatus.BAD_GATEWAY, args);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, "EXTERNAL_SERVICE_ERROR", HttpStatus.BAD_GATEWAY, cause);
    }

    public ExternalServiceException(String message, Throwable cause, Object... args) {
        super(message, "EXTERNAL_SERVICE_ERROR", HttpStatus.BAD_GATEWAY, cause, args);
    }
}
