package io.dav033.maroconstruction.dto;

import java.time.LocalDate;

public record ProjectWithLeadDTO(
        Long id,
        String projectName,
        String overview,
        String projectStatus,
        String invoiceStatus,
        Boolean quickbooks,
        LocalDate startDate,
        LocalDate endDate,
        Long leadId,
        String leadNumber,
        String leadName,
        String location,
        Long contactId,
        String contactName,
        String customerName
) {}
