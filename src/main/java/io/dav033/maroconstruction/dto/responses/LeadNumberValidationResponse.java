package io.dav033.maroconstruction.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadNumberValidationResponse {
    private boolean valid;
    private String reason;
}
