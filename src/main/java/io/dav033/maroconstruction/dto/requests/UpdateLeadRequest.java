package io.dav033.maroconstruction.dto.requests;

import io.dav033.maroconstruction.dto.Leads;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLeadRequest {
    private Leads lead;
}
