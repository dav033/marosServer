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

@Component
public class CustomFieldsBuilder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomFieldsBuilder.class);

    private final ClickUpRoutingService routingService;
    private final ContactsService contactsService;

    public CustomFieldsBuilder(ClickUpRoutingService routingService, ContactsService contactsService) {
        this.routingService = routingService;
        this.contactsService = contactsService;
    }

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
        Contacts contact = null;
        try {
            contact = contactsService.getContactById(dto.getContactId());
        } catch (Exception ignored) {}

        String contactName  = contact != null ? Optional.ofNullable(contact.getName()).orElse("") : "";
        String contactEmail = contact != null ? Optional.ofNullable(contact.getEmail()).orElse("") : "";
        String contactPhone = contact != null ? Optional.ofNullable(contact.getPhone()).orElse("") : "";

        addField(fields, f != null ? f.getContactNameId() : null,  contactName,   true);
        addField(fields, f != null ? f.getCustomerNameId() : null, contactName,   true);
        addField(fields, f != null ? f.getEmailId() : null,        contactEmail,  true);
        
        // Para campos de tipo "phone" en ClickUp, formatear con código de país
        String formattedPhone = formatPhoneForClickUp(contactPhone);
        addField(fields, f != null ? f.getPhoneId() : null,        formattedPhone,  true);
        addField(fields, f != null ? f.getPhoneTextId() : null,    contactPhone,  true);
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
        if (org.springframework.util.StringUtils.hasText(locationId)) {
            Object locationValue = null;
            if (addr != null && !addr.isBlank()) {
                java.util.Map<String, Object> loc = new java.util.HashMap<>();
                loc.put("address", addr.trim());
                locationValue = loc;
            }
            addField(fields, locationId, locationValue, true);
        }
        addField(fields, addrTextId, addr, true);

        return fields;
    }

    private void addField(List<ClickUpTaskRequest.CustomField> out, String fieldId, Object value, boolean clearIfMissing) {
        if (fieldId == null || fieldId.isBlank()) return;
        if (value == null) {
            if (clearIfMissing) {
                out.add(ClickUpTaskRequest.CustomField.builder()
                    .id(fieldId)
                    .value(null) 
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

    /**
     * Formatea un número de teléfono para ClickUp (campos tipo "phone").
     * ClickUp requiere formato internacional con código de país.
     * Si el número ya tiene +, se mantiene. Si no, se asume +1 (USA).
     */
    private String formatPhoneForClickUp(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        // Remover espacios, guiones, paréntesis, puntos
        String cleaned = phone.replaceAll("[\\s\\-().]+", "");
        
        // Si ya tiene +, retornar tal cual
        if (cleaned.startsWith("+")) {
            return cleaned;
        }
        
        // Si empieza con 1 y tiene 11 dígitos (formato USA), agregar +
        if (cleaned.startsWith("1") && cleaned.length() == 11) {
            return "+" + cleaned;
        }
        
        // Si tiene 10 dígitos, asumir USA y agregar +1
        if (cleaned.length() == 10 && cleaned.matches("\\d{10}")) {
            return "+1" + cleaned;
        }
        
        // Si tiene otros dígitos, asumir que necesita +1
        if (cleaned.matches("\\d+")) {
            return "+1" + cleaned;
        }
        
        // Si no es un número válido, retornar null para evitar el error
        log.warn("Teléfono en formato inválido para ClickUp: '{}'. Se enviará como null.", phone);
        return null;
    }
}
