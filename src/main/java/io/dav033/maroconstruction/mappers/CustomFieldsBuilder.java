package io.dav033.maroconstruction.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import io.dav033.maroconstruction.config.ClickUpRoutingService;
import io.dav033.maroconstruction.enums.LeadType;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.dto.LeadPayloadDto;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.services.ContactsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomFieldsBuilder {

    private final ClickUpRoutingService routingService;
    private final ContactsService contactsService;

    public List<ClickUpTaskRequest.CustomField> build(LeadPayloadDto dto) {
        List<ClickUpTaskRequest.CustomField> fields = new ArrayList<>();

        LeadType type = LeadType.valueOf(dto.getLeadType().trim().toUpperCase());
        String number = dto.getLeadNumber();
        var r = routingService.route(type);
        var f = r.getFields();
        String leadNumberFieldId = routingService.resolveLeadNumberFieldId(type);
        if (!org.springframework.util.StringUtils.hasText(number)) {
            throw new IllegalStateException("LeadNumber vacío al crear tarea (leadType=" + type + ")");
        }
        if (!org.springframework.util.StringUtils.hasText(leadNumberFieldId)) {
            throw new IllegalStateException("No se pudo resolver leadNumberId para " + type + ". Configure el ID o habilite auto-descubrimiento.");
        }
        fields.add(ClickUpTaskRequest.CustomField.builder()
            .id(leadNumberFieldId)
            .value(number)
            .build());
        log.debug("FIELD → {} = {} (leadNumber)", leadNumberFieldId, number);

        // Usar datos del contacto si existen
        Contacts contact = null;
        try {
            contact = contactsService.getContactById(dto.getContactId());
        } catch (Exception ignored) {}

        String contactName  = contact != null ? Optional.ofNullable(contact.getName()).orElse("") : "";
        String companyName  = contact != null ? Optional.ofNullable(contact.getCompanyName()).orElse("") : "";
        String contactEmail = contact != null ? Optional.ofNullable(contact.getEmail()).orElse("") : "";
        String contactPhone = contact != null ? Optional.ofNullable(contact.getPhone()).orElse("") : "";

        addField(fields, f != null ? f.getContactNameId() : null,  contactName,   true);
        addField(fields, f != null ? f.getCustomerNameId() : null, companyName,   true);
        addField(fields, f != null ? f.getEmailId() : null,        contactEmail,  true);
        addField(fields, f != null ? f.getPhoneId() : null,        contactPhone,  true);
        addField(fields, f != null ? f.getPhoneTextId() : null,    contactPhone,  true);

        // Dirección: location (objeto) y/o short_text
        String addr = dto.getLocation();
        String addrTextId = f != null ? f.getLocationTextId() : null;
        String locationId = null;
        if (f != null) {
            try {
                var field = f.getClass().getDeclaredField("locationId");
                field.setAccessible(true);
                locationId = (String) field.get(f);
            } catch (Exception ignored) {}
        }

        // 1) Campo tipo location (objeto)
        if (org.springframework.util.StringUtils.hasText(locationId)) {
            Object locationValue = null;
            if (addr != null && !addr.isBlank()) {
                java.util.Map<String, Object> loc = new java.util.HashMap<>();
                loc.put("address", addr.trim());
                // Si tuviera coordenadas:
                // loc.put("lat", lead.getLatitude());
                // loc.put("lng", lead.getLongitude());
                locationValue = loc;
            }
            addField(fields, locationId, locationValue, true);
        }

        // 2) Campo short_text (opcional)
        addField(fields, addrTextId, addr, true);

        return fields;
    }

    private void addField(List<ClickUpTaskRequest.CustomField> out, String fieldId, Object value, boolean clearIfMissing) {
        if (fieldId == null || fieldId.isBlank()) return;
        if (value == null) {
            if (clearIfMissing) {
                out.add(ClickUpTaskRequest.CustomField.builder()
                    .id(fieldId)
                    .value(null) // LIMPIA el field en ClickUp
                    .build());
            }
            return;
        }
        if (value instanceof String) {
            String str = (String) value;
            if (str.trim().isEmpty()) {
                if (clearIfMissing) {
                    out.add(ClickUpTaskRequest.CustomField.builder()
                        .id(fieldId)
                        .value(null)
                        .build());
                }
                return;
            }
            out.add(ClickUpTaskRequest.CustomField.builder()
                .id(fieldId)
                .value(str.trim())
                .build());
        } else {
            out.add(ClickUpTaskRequest.CustomField.builder()
                .id(fieldId)
                .value(value)
                .build());
        }
    }
}
