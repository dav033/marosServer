package io.dav033.maroconstruction.dto.requests;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.dto.Leads;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLeadByNewContactRequest {
    private Leads lead;
    private Contacts contact;
}
