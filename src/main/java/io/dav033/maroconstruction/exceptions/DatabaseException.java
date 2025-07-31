package io.dav033.maroconstruction.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción para errores de base de datos
 */
public class DatabaseException extends BaseException {

    public DatabaseException(String message) {
        super(message, "DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DatabaseException(String message, Object... args) {
        super(message, "DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, args);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, "DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public DatabaseException(String message, Throwable cause, Object... args) {
        super(message, "DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, cause, args);
    }
}
