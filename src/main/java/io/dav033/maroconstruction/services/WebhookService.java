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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Servicio que procesa los webhooks de Supabase y los convierte en tareas de ClickUp
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {
    
    private final ClickUpService clickUpService;
    private final ClickUpConfig clickUpConfig;
    private final ContactsService contactsService;
    
    /**
     * Procesa un webhook de Supabase y crea una tarea en ClickUp si es necesario
     * 
     * @param payload Datos del webhook de Supabase
     * @return Respuesta de ClickUp si se creó una tarea, null si no
     */
    public ClickUpTaskResponse processSupabaseWebhook(SupabaseWebhookPayload payload) {
        log.info("Procesando webhook de Supabase: tabla={}, tipo={}", 
            payload.getTable(), payload.getType());
        
        // Solo procesamos inserts en la tabla leads
        if (!"INSERT".equals(payload.getType()) || !"leads".equals(payload.getTable())) {
            log.debug("Webhook ignorado: no es INSERT en tabla leads");
            return null;
        }
        
        // Verificar que ClickUp está configurado
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
            throw e; // Re-lanzar para que el controlador maneje el error
        }
    }
    
    /**
     * Mapea los datos de un lead de Supabase a una tarea de ClickUp
     * 
     * @param leadData Datos del lead desde Supabase
     * @return Request para crear tarea en ClickUp
     */
    private ClickUpTaskRequest mapLeadToClickUpTask(Map<String, Object> leadData) {
        // Extraer datos del lead
        String leadName = (String) leadData.get("name");
        String leadNumber = (String) leadData.get("lead_number");
        String location = (String) leadData.get("location");
        String startDate = (String) leadData.get("start_date");
        String status = (String) leadData.get("status");
        String leadType = (String) leadData.get("lead_type");
        
        // Obtener contact_id para buscar datos reales
        Long contactId = getLongValue(leadData.get("contact_id"));
        
        // Obtener datos del contacto para incluir en la descripción
        Contacts contactData = null;
        String contactInfo = "";
        if (contactId != null) {
            try {
                contactData = contactsService.getContactById(contactId);
                if (contactData != null) {
                    StringBuilder contactBuilder = new StringBuilder("\\n**Información del Contacto:**\\n");
                    
                    // Solo agregar campos que realmente existen
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
                    
                    // Solo usar la información si hay al menos un campo
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
        
        // Construir custom fields con datos reales del contacto (manejo de errores por workspace ID)
        List<ClickUpTaskRequest.CustomField> customFields = new ArrayList<>();
        try {
            customFields = buildCustomFields(leadData, contactId);
            log.info("✅ Custom fields construidos exitosamente: {} campos", customFields.size());
        } catch (Exception e) {
            log.warn("⚠️ No se pudieron construir custom fields (workspace específico): {}", e.getMessage());
            customFields = new ArrayList<>(); // Lista vacía como fallback
        }
        
        // Construir nombre de la tarea
        String taskName = String.format("Lead: %s (%s)", leadName, leadNumber);
        
        // Construir descripción
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
        
        // Agregar información del contacto a la descripción
        description.append(contactInfo);
        
        description.append("\\n*Tarea creada automáticamente desde Supabase*");
        
        // Crear tags basados en el tipo de lead y status
        List<String> tags = Arrays.asList(
            "lead",
            leadType != null ? leadType.toLowerCase() : "construction",
            "automated"
        );
        
        return ClickUpTaskRequest.builder()
            .name(taskName)
            .description(description.toString())
            .tags(tags)
            // .status(mapStatusToClickUp(status))  // Comentado para no enviar status
            .priority(clickUpConfig.getDefaultPriority())
            .customFields(customFields)
            .build();
    }
    
    /**
     * Mapea el status del lead al status de ClickUp
     */
    private String mapStatusToClickUp(String leadStatus) {
        if (leadStatus == null) {
            return clickUpConfig.getDefaultStatus();
        }
        
        switch (leadStatus.toUpperCase()) {
            case "TO_DO":
                return "to do";
            case "IN_PROGRESS":
                return "in progress";
            case "COMPLETED":
                return "complete";
            case "CANCELLED":
                return "cancelled";
            default:
                return clickUpConfig.getDefaultStatus();
        }
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
        String leadName = leadData.get("lead_name") != null ? leadData.get("lead_name").toString() : null;
        String leadType = leadData.get("lead_type") != null ? leadData.get("lead_type").toString() : null;
        String location = leadData.get("location") != null ? leadData.get("location").toString() : null;
        String phoneFromLead = leadData.get("phone") != null ? leadData.get("phone").toString() : null;
        
        // 🎯 USAR DATOS REALES DEL CONTACTO CUANDO ESTÉN DISPONIBLES
        String contactName = contactData != null ? contactData.getName() : null;
        String contactPhone = contactData != null ? contactData.getPhone() : phoneFromLead;
        String contactEmail = contactData != null ? contactData.getEmail() : null;
        String contactAddress = contactData != null ? contactData.getAddress() : null;
        String companyName = contactData != null ? contactData.getCompanyName() : null;
        
        // 👤 Contact Name (524a8b7c-cfb7-4361-886e-59a019f8c5b5) - SOLO SI EXISTE NOMBRE REAL
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
        
        // 🏢 Customer Name (c8dbf709-6ef9-479f-a915-b20518ac30e6) - SOLO SI EXISTE COMPAÑÍA
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
        
        // 📧 Email (f2220992-2039-4a6f-9717-b53ede8f5ec1) - SOLO SI EXISTE EMAIL REAL
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
        
        // 📞 Phone Number (Text) (9edb199d-5c9f-404f-84f1-ad6a78597175) - TELÉFONO REAL DEL CONTACTO
        if (contactPhone != null && !contactPhone.trim().isEmpty()) {
            ClickUpTaskRequest.CustomField phoneNumberField = ClickUpTaskRequest.CustomField.builder()
                .id("9edb199d-5c9f-404f-84f1-ad6a78597175")
                .value(contactPhone.trim())
                .build();
            customFields.add(phoneNumberField);
            log.info("✅ Phone Number (Text) field agregado: Value={}", contactPhone);
        }
        
        // 📞 Client Contact Number (f94558c8-3c7a-48cb-999c-c697b7842ddf) - TELÉFONO REAL DEL CONTACTO
        if (contactPhone != null && !contactPhone.trim().isEmpty()) {
            ClickUpTaskRequest.CustomField clientContactNumberField = ClickUpTaskRequest.CustomField.builder()
                .id("f94558c8-3c7a-48cb-999c-c697b7842ddf")
                .value(contactPhone.trim())
                .build();
            customFields.add(clientContactNumberField);
            log.info("✅ Client Contact Number field agregado: Value={}", contactPhone);
        }
        
        // 🏠 Address (Text) (401a9851-6f11-4043-b577-4c7b3f03fb03) - DIRECCIÓN DEL LEAD (location)
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
        
        // 🎯 Lead # (53d6e312-0f63-40ba-8f87-1f3092d8b322) - LEAD NUMBER
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
    
    /**
     * Convierte un Object a Long de forma segura
     */
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
