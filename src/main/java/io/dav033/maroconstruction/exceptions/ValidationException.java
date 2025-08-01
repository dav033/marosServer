package io.dav033.maroconstruction.exceptions;

import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String message, Object... args) {
        super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST, args);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST, cause);
    }

    public ValidationException(String message, Throwable cause, Object... args) {
        super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST, cause, args);
    }
}
