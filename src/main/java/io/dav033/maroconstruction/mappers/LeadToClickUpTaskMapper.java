package io.dav033.maroconstruction.mappers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import io.dav033.maroconstruction.config.ClickUpConfig;
import io.dav033.maroconstruction.dto.LeadPayloadDto;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;

@Mapper(componentModel = "spring", uses = { CustomFieldsBuilder.class, ContactInfoFormatter.class })
public abstract class LeadToClickUpTaskMapper {

    @Autowired
    protected ClickUpConfig clickUpConfig;
    @Autowired
    protected ContactInfoFormatter contactInfoFormatter;
    @Autowired
    protected CustomFieldsBuilder customFieldsBuilder;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Mapping(target = "name", expression = "java(String.format(\"Lead: %s (%s)\", dto.getName(), dto.getLeadNumber()))")
    @Mapping(target = "description", expression = "java(buildDescription(dto))")
    @Mapping(target = "tags", expression = "java(buildTags(dto.getLeadType()))")
    @Mapping(target = "priority", expression = "java(clickUpConfig.getDefaultPriority())")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "customFields", expression = "java(customFieldsBuilder.build(dto))")
    @Mapping(target = "startDate", expression = "java(convertDateStringToTimestamp(dto.getStartDate()))")
    @Mapping(target = "assignees", ignore = true)
    @Mapping(target = "dueDate", ignore = true)
    @Mapping(target = "timeEstimate", ignore = true)
    public abstract ClickUpTaskRequest toClickUpTask(LeadPayloadDto dto);

    protected String buildDescription(LeadPayloadDto dto) {
        String fecha = Optional.ofNullable(dto.getStartDate())
                .map(LocalDate::parse)
                .map(d -> d.format(DATE_FMT))
                .orElse(dto.getStartDate());

        String contacto = contactInfoFormatter.formatFor(dto.getContactId());

        return String.join("\n",
                "**New Lead Created**",
                "",
                "**Details:**",
                String.format("- **Lead Number:** %s", dto.getLeadNumber()),
                String.format("- **Name:** %s", dto.getName()),
                dto.getLocation() != null && !dto.getLocation().isBlank()
                        ? String.format("- **Location:** %s", dto.getLocation())
                        : "",
                dto.getStartDate() != null
                        ? String.format("- **Start Date:** %s", fecha)
                        : "",
                dto.getLeadType() != null
                        ? String.format("- **Type:** %s", dto.getLeadType())
                        : "",
                contacto,
                "",
                "*Task created automatically from Supabase*");
    }

    protected List<String> buildTags(String leadType) {
        return List.of(
                "lead",
                Optional.ofNullable(leadType).map(String::toLowerCase).orElse("construction"),
                "automated");
    }

    protected Long convertDateStringToTimestamp(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            LocalDate date = LocalDate.parse(dateString);
            return date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }
}