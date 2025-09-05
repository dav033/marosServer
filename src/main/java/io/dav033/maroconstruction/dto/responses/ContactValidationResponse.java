package io.dav033.maroconstruction.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactValidationResponse {
    private boolean nameAvailable;
    private boolean emailAvailable;
    private boolean phoneAvailable;
    private String nameReason;
    private String emailReason;
    private String phoneReason;
}
