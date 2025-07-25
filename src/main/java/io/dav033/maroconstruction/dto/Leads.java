package io.dav033.maroconstruction.dto;

import io.dav033.maroconstruction.enums.LeadStatus;
import io.dav033.maroconstruction.enums.LeadType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leads {
    private Long id;
    private String leadNumber;
    private String name;
    private LocalDate startDate;
    private String location;
    private LeadStatus status;
    private Contacts contact;
    private ProjectType projectType;
    private LeadType leadType;
}
