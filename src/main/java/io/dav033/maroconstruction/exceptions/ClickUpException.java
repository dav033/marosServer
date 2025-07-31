package io.dav033.maroconstruction.exceptions;

/**
 * Excepción específica para errores de ClickUp
 */
public class ClickUpException extends ExternalServiceException {

    private final int statusCode;
    private final String responseBody;

    public ClickUpException(String message) {
        super(message);
        this.statusCode = 0;
        this.responseBody = null;
    }

    public ClickUpException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.responseBody = null;
    }

    public ClickUpException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
