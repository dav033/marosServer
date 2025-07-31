package io.dav033.maroconstruction.dto;

import lombok.Data;

@Data
public class LeadPayloadDto {
    private Integer id;
    private String leadNumber;
    private String name;
    private String leadType;
    private String location;
    private String startDate;
    private String status;
    private Long contactId;
}
