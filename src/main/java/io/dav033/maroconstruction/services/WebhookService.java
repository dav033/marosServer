package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.config.ClickUpConfig;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
            .status(mapStatusToClickUp(status))
            .priority(clickUpConfig.getDefaultPriority())
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
}
