package io.dav033.maroconstruction.dto.requests;

import io.dav033.maroconstruction.enums.LeadType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetLeadsByTypeRequest {
    private LeadType type;
}
