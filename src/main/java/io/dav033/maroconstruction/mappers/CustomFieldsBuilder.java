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
        var r = routingService.route(type);

        // Usar datos del contacto si existen
        Contacts contact = null;
        try {
            contact = contactsService.getContactById(dto.getContactId());
        } catch (Exception ignored) {}

        String contactName  = contact != null ? Optional.ofNullable(contact.getName()).orElse("") : "";
        String companyName  = contact != null ? Optional.ofNullable(contact.getCompanyName()).orElse("") : "";
        String contactEmail = contact != null ? Optional.ofNullable(contact.getEmail()).orElse("") : "";
        String contactPhone = contact != null ? Optional.ofNullable(contact.getPhone()).orElse("") : "";

    addField(fields, r.getContactNameId(),  contactName,   "👤 Contact Name");
    addField(fields, r.getCompanyNameId(),  companyName,   "🏢 Company Name");
    addField(fields, r.getEmailId(),        contactEmail,  "📧 Contact Email");
    addField(fields, r.getPhoneId(),        contactPhone,  "📞 Contact Phone");
    addField(fields, r.getLocationTextId(), dto.getLocation(), "📍 Location");
    addField(fields, r.getLeadNumberId(),   dto.getLeadNumber(), "🔢 Lead Number");
    return fields;
    }

    private void addField(List<ClickUpTaskRequest.CustomField> list, String fieldId, String value, String description) {
        if (fieldId == null) return;
        String finalValue = value != null ? value.trim() : "";
        list.add(ClickUpTaskRequest.CustomField.builder()
            .id(fieldId)
            .value(finalValue)
            .build());
    }
}
