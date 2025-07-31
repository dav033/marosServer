package io.dav033.maroconstruction.mappers;

import java.time.LocalDate;
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
    @Mapping(target = "customFields", expression = "java(customFieldsBuilder.build(dto))")
    public abstract ClickUpTaskRequest toClickUpTask(LeadPayloadDto dto);

    protected String buildDescription(LeadPayloadDto dto) {
        String fecha = Optional.ofNullable(dto.getStartDate())
                .map(LocalDate::parse)
                .map(d -> d.format(DATE_FMT))
                .orElse(dto.getStartDate());

        String contacto = contactInfoFormatter.formatFor(dto.getContactId());

        return String.join("\n",
                "**Nuevo Lead Creado**",
                "",
                "**Detalles:**",
                String.format("- **Número de Lead:** %s", dto.getLeadNumber()),
                String.format("- **Nombre:** %s", dto.getName()),
                dto.getLocation() != null && !dto.getLocation().isBlank()
                        ? String.format("- **Ubicación:** %s", dto.getLocation())
                        : "",
                dto.getStartDate() != null
                        ? String.format("- **Fecha de Inicio:** %s", fecha)
                        : "",
                dto.getLeadType() != null
                        ? String.format("- **Tipo:** %s", dto.getLeadType())
                        : "",
                contacto,
                "",
                "*Tarea creada automáticamente desde Supabase*");
    }

    protected List<String> buildTags(String leadType) {
        return List.of(
                "lead",
                Optional.ofNullable(leadType).map(String::toLowerCase).orElse("construction"),
                "automated");
    }
}