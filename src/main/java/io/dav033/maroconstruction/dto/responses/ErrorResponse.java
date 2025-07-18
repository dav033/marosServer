package io.dav033.maroconstruction.dto.responses;

import java.time.LocalDateTime;


public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        String path
) {
}
