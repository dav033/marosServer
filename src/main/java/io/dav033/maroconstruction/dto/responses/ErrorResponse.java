package io.dav033.maroconstruction.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
                LocalDateTime timestamp,
                int status,
                String error,
                String message,
                String errorCode,
                String path,
                String traceId,
                Map<String, Object> details,
                List<FieldError> fieldErrors) {

        public record FieldError(
                        String field,
                        Object rejectedValue,
                        String message) {
                public static FieldErrorBuilder builder() { return new FieldErrorBuilder(); }
                public static final class FieldErrorBuilder {
                        private String field;
                        private Object rejectedValue;
                        private String message;
                        public FieldErrorBuilder field(String v) { this.field = v; return this; }
                        public FieldErrorBuilder rejectedValue(Object v) { this.rejectedValue = v; return this; }
                        public FieldErrorBuilder message(String v) { this.message = v; return this; }
                        public FieldError build() { return new FieldError(field, rejectedValue, message); }
                }
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
                private LocalDateTime timestamp;
                private int status;
                private String error;
                private String message;
                private String errorCode;
                private String path;
                private String traceId;
                private Map<String, Object> details;
                private List<FieldError> fieldErrors;

                public Builder timestamp(LocalDateTime v) { this.timestamp = v; return this; }
                public Builder status(int v) { this.status = v; return this; }
                public Builder error(String v) { this.error = v; return this; }
                public Builder message(String v) { this.message = v; return this; }
                public Builder errorCode(String v) { this.errorCode = v; return this; }
                public Builder path(String v) { this.path = v; return this; }
                public Builder traceId(String v) { this.traceId = v; return this; }
                public Builder details(Map<String, Object> v) { this.details = v; return this; }
                public Builder fieldErrors(List<FieldError> v) { this.fieldErrors = v; return this; }
                public ErrorResponse build() {
                        return new ErrorResponse(timestamp, status, error, message, errorCode, path, traceId, details, fieldErrors);
                }
        }

        public static ErrorResponse of(int status, String error, String message, String path) {
                return ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(status)
                                .error(error)
                                .message(message)
                                .path(path)
                                .build();
        }

        public static ErrorResponse of(int status, String error, String message, String errorCode, String path) {
                return ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(status)
                                .error(error)
                                .message(message)
                                .errorCode(errorCode)
                                .path(path)
                                .build();
        }
}
