package io.dav033.maroconstruction.dto;

import io.dav033.maroconstruction.enums.InvoiceStatus;
import io.dav033.maroconstruction.enums.ProjectStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    private Long id;
    private String projectName;
    private String overview;
    private List<Float> payments;
    private ProjectStatus projectStatus;
    private InvoiceStatus invoiceStatus;
    private Boolean quickbooks;
    private LocalDate startDate;
    private LocalDate endDate;
    private Leads lead;
}
