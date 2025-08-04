package io.dav033.maroconstruction.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

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

    private final ContactsService contactsService;

    public List<ClickUpTaskRequest.CustomField> build(LeadPayloadDto dto) {
        List<ClickUpTaskRequest.CustomField> fields = new ArrayList<>();

        log.info("Building custom fields for lead: leadNumber={}, contactId={}", 
                dto.getLeadNumber(), dto.getContactId());

        // Intentamos obtener datos del contacto si viene contactId
        Contacts contact = null;
        if (dto.getContactId() != null) {
            try {
                contact = contactsService.getContactById(dto.getContactId());
                log.info("Found contact for update: id={}, name={}, email={}, phone={}, company={}", 
                        contact.getId(), contact.getName(), contact.getEmail(), 
                        contact.getPhone(), contact.getCompanyName());
            } catch (Exception e) { 
                log.warn("Could not find contact with id={}: {}", dto.getContactId(), e.getMessage());
            }
        } else {
            log.info("No contactId provided for lead: {}", dto.getLeadNumber());
        }

        String leadNumber   = Optional.ofNullable(dto.getLeadNumber()).orElse("");
        String location     = Optional.ofNullable(dto.getLocation()).orElse("");
        String contactName  = contact != null ? contact.getName() : "";
        String contactEmail = contact != null ? contact.getEmail() : "";
        String contactPhone = contact != null ? contact.getPhone() : "";
        String companyName  = contact != null ? contact.getCompanyName() : "";

        log.info("Mapped contact fields for ClickUp: name='{}', email='{}', phone='{}', company='{}'", 
                contactName, contactEmail, contactPhone, companyName);

        // Agregar cada campo si tiene valor
        addFieldIfPresent(fields, "524a8b7c-cfb7-4361-886e-59a019f8c5b5", contactName);
        addFieldIfPresent(fields, "c8dbf709-6ef9-479f-a915-b20518ac30e6", companyName);
        addFieldIfPresent(fields, "f2220992-2039-4a6f-9717-b53ede8f5ec1", contactEmail);
        addFieldIfPresent(fields, "9edb199d-5c9f-404f-84f1-ad6a78597175", contactPhone);
        addFieldIfPresent(fields, "f94558c8-3c7a-48cb-999c-c697b7842ddf", contactPhone);
        addFieldIfPresent(fields, "401a9851-6f11-4043-b577-4c7b3f03fb03", location);
        addFieldIfPresent(fields, "53d6e312-0f63-40ba-8f87-1f3092d8b322", leadNumber);

        log.info("Built {} custom fields for ClickUp task update", fields.size());
        return fields;
    }

    private void addFieldIfPresent(
            List<ClickUpTaskRequest.CustomField> list,
            String fieldId,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            list.add(ClickUpTaskRequest.CustomField.builder()
                .id(fieldId)
                .value(value.trim())
                .build());
        }
    }
}
