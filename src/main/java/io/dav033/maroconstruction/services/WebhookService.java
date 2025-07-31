package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.config.ClickUpConfig;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;
import io.dav033.maroconstruction.dto.Contacts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {
    
    private final ClickUpService clickUpService;
    private final ClickUpConfig clickUpConfig;
    private final ContactsService contactsService;
    
    public ClickUpTaskResponse processSupabaseWebhook(SupabaseWebhookPayload payload) {
        log.info("Procesando webhook de Supabase: tabla={}, tipo={}", 
            payload.getTable(), payload.getType());
        
        if (!"INSERT".equals(payload.getType()) || !"leads".equals(payload.getTable())) {
            log.debug("Webhook ignorado: no es INSERT en tabla leads");
            return null;
        }
        
        if (!clickUpService.isConfigured()) {
            log.warn("ClickUp no está configurado correctamente. Saltando creación de tarea.");
            return null;
        }
        
        try {
            Map<String, Object> leadData = payload.getRecord();
            ClickUpTaskRequest taskRequest = mapLeadToClickUpTask(leadData);
            return clickUpService.createTask(taskRequest);
        } catch (Exception e) {
            log.error("Error procesando webhook de Supabase", e);
            throw e;
        }
    }
    
    private ClickUpTaskRequest mapLeadToClickUpTask(Map<String, Object> leadData) {
        String leadName = (String) leadData.get("name");
        String leadNumber = (String) leadData.get("lead_number");
        String location = (String) leadData.get("location");
        String startDate = (String) leadData.get("start_date");
        String leadType = (String) leadData.get("lead_type");
        
        Long contactId = getLongValue(leadData.get("contact_id"));
        
        Contacts contactData = null;
        String contactInfo = "";
        if (contactId != null) {
            try {
                contactData = contactsService.getContactById(contactId);
                if (contactData != null) {
                    StringBuilder contactBuilder = new StringBuilder("\\n**Información del Contacto:**\\n");
                    
                    if (contactData.getCompanyName() != null && !contactData.getCompanyName().trim().isEmpty()) {
                        contactBuilder.append("- **Empresa:** ").append(contactData.getCompanyName()).append("\\n");
                    }
                    if (contactData.getName() != null && !contactData.getName().trim().isEmpty()) {
                        contactBuilder.append("- **Contacto:** ").append(contactData.getName()).append("\\n");
                    }
                    if (contactData.getEmail() != null && !contactData.getEmail().trim().isEmpty()) {
                        contactBuilder.append("- **Email:** ").append(contactData.getEmail()).append("\\n");
                    }
                    if (contactData.getPhone() != null && !contactData.getPhone().trim().isEmpty()) {
                        contactBuilder.append("- **Teléfono:** ").append(contactData.getPhone()).append("\\n");
                    }
                    
                    String contactContent = contactBuilder.toString();
                    if (!contactContent.equals("\\n**Información del Contacto:**\\n")) {
                        contactInfo = contactContent;
                    }
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener información del contacto ID {}: {}", contactId, e.getMessage());
                contactInfo = "\\n**Contacto ID:** " + contactId + "\\n";
            }
        }
        
        List<ClickUpTaskRequest.CustomField> customFields = new ArrayList<>();
        try {
            customFields = buildCustomFields(leadData, contactId);
            log.info("✅ Custom fields construidos exitosamente: {} campos", customFields.size());
        } catch (Exception e) {
            log.warn("⚠️ No se pudieron construir custom fields (workspace específico): {}", e.getMessage());
            customFields = new ArrayList<>();
        }
        
        String taskName = String.format("Lead: %s (%s)", leadName, leadNumber);
        
        StringBuilder description = new StringBuilder();
        description.append("**Nuevo Lead Creado**\\n\\n");
        description.append("**Detalles:**\\n");
        description.append("- **Número de Lead:** ").append(leadNumber).append("\\n");
        description.append("- **Nombre:** ").append(leadName).append("\\n");
        
        if (location != null && !location.isEmpty()) {
            description.append("- **Ubicación:** ").append(location).append("\\n");
        }
        
        if (startDate != null) {
            description.append("- **Fecha de Inicio:** ").append(formatDate(startDate)).append("\\n");
        }
        
        if (leadType != null) {
            description.append("- **Tipo:** ").append(leadType).append("\\n");
        }
        
        description.append(contactInfo);
        description.append("\\n*Tarea creada automáticamente desde Supabase*");
        
        List<String> tags = Arrays.asList(
            "lead",
            leadType != null ? leadType.toLowerCase() : "construction",
            "automated"
        );
        
        return ClickUpTaskRequest.builder()
            .name(taskName)
            .description(description.toString())
            .tags(tags)
            .priority(clickUpConfig.getDefaultPriority())
            .customFields(customFields)
            .build();
    }
    
    /**
     * Formatea una fecha para mostrar en la descripción
     */
    private String formatDate(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString);
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return dateString; // Devolver el original si no se puede parsear
        }
    }
    
    /**
     * Construye los custom fields para ClickUp con datos reales del contacto
     */
    private List<ClickUpTaskRequest.CustomField> buildCustomFields(Map<String, Object> leadData, Long contactId) {
        List<ClickUpTaskRequest.CustomField> customFields = new ArrayList<>();
        
        // Obtener datos del contacto real si existe contactId
        Contacts contactData = null;
        if (contactId != null) {
            try {
                contactData = contactsService.getContactById(contactId);
                log.info("✅ Datos del contacto obtenidos: nombre={}, email={}, phone={}, address={}", 
                    contactData.getName(), contactData.getEmail(), contactData.getPhone(), contactData.getAddress());
            } catch (Exception e) {
                log.warn("⚠️ No se pudo obtener el contacto ID {}: {}", contactId, e.getMessage());
            }
        }
        
        // Extraer datos del lead para campos básicos
        String leadNumber = leadData.get("lead_number") != null ? leadData.get("lead_number").toString() : null;
        String location = leadData.get("location") != null ? leadData.get("location").toString() : null;
        String phoneFromLead = leadData.get("phone") != null ? leadData.get("phone").toString() : null;
        
        // USAR DATOS REALES DEL CONTACTO CUANDO ESTÉN DISPONIBLES
        String contactName = contactData != null ? contactData.getName() : null;
        String contactPhone = contactData != null ? contactData.getPhone() : phoneFromLead;
        String contactEmail = contactData != null ? contactData.getEmail() : null;
        String companyName = contactData != null ? contactData.getCompanyName() : null;
        
        if (contactName != null && !contactName.trim().isEmpty() && !contactName.equals("Cliente por definir")) {
            ClickUpTaskRequest.CustomField contactNameField = ClickUpTaskRequest.CustomField.builder()
                .id("524a8b7c-cfb7-4361-886e-59a019f8c5b5")
                .value(contactName.trim())
                .build();
            customFields.add(contactNameField);
            log.info("✅ Contact Name field agregado: Value={}", contactName);
        } else {
            log.info("⚠️ Contact Name omitido - no hay nombre real disponible");
        }
        
        if (companyName != null && !companyName.trim().isEmpty()) {
            ClickUpTaskRequest.CustomField customerNameField = ClickUpTaskRequest.CustomField.builder()
                .id("c8dbf709-6ef9-479f-a915-b20518ac30e6")
                .value(companyName.trim())
                .build();
            customFields.add(customerNameField);
            log.info("✅ Customer Name field agregado: Value={}", companyName);
        } else {
            log.info("⚠️ Customer Name omitido - no hay companyName disponible");
        }
        
        if (contactEmail != null && !contactEmail.trim().isEmpty()) {
            ClickUpTaskRequest.CustomField emailField = ClickUpTaskRequest.CustomField.builder()
                .id("f2220992-2039-4a6f-9717-b53ede8f5ec1")
                .value(contactEmail.trim())
                .build();
            customFields.add(emailField);
            log.info("✅ Email field agregado: Value={}", contactEmail);
        } else {
            log.info("⚠️ Email omitido - no hay email disponible");
        }
        
        if (contactPhone != null && !contactPhone.trim().isEmpty()) {
            ClickUpTaskRequest.CustomField phoneNumberField = ClickUpTaskRequest.CustomField.builder()
                .id("9edb199d-5c9f-404f-84f1-ad6a78597175")
                .value(contactPhone.trim())
                .build();
            customFields.add(phoneNumberField);
            log.info("✅ Phone Number (Text) field agregado: Value={}", contactPhone);
        }
        
        if (contactPhone != null && !contactPhone.trim().isEmpty()) {
            ClickUpTaskRequest.CustomField clientContactNumberField = ClickUpTaskRequest.CustomField.builder()
                .id("f94558c8-3c7a-48cb-999c-c697b7842ddf")
                .value(contactPhone.trim())
                .build();
            customFields.add(clientContactNumberField);
            log.info("✅ Client Contact Number field agregado: Value={}", contactPhone);
        }
        
        if (location != null && !location.trim().isEmpty()) {
            ClickUpTaskRequest.CustomField addressTextField = ClickUpTaskRequest.CustomField.builder()
                .id("401a9851-6f11-4043-b577-4c7b3f03fb03")
                .value(location.trim())
                .build();
            customFields.add(addressTextField);
            log.info("✅ Address (Text) field agregado (location del lead): Value={}", location);
        } else {
            log.info("⚠️ Address omitido - no hay location en el lead");
        }
        
        if (leadNumber != null && !leadNumber.trim().isEmpty()) {
            ClickUpTaskRequest.CustomField leadNumberField = ClickUpTaskRequest.CustomField.builder()
                .id("53d6e312-0f63-40ba-8f87-1f3092d8b322")
                .value(leadNumber.trim())
                .build();
            customFields.add(leadNumberField);
            log.info("✅ Lead # field agregado: Value={}", leadNumber);
        }
        
        log.info("🎯 Total custom fields construidos: {}", customFields.size());
        return customFields;
    }
    
    private Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
