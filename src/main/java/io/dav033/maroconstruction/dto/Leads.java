package io.dav033.maroconstruction.dto;

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
    private String status;
    private Contacts contact;
    private ProjectType projectType;
}
