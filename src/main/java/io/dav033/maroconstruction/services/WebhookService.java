package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;
import io.dav033.maroconstruction.mappers.LeadToClickUpTaskMapper;
import io.dav033.maroconstruction.mappers.SupabasePayloadMapper;
import io.dav033.maroconstruction.dto.LeadPayloadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final ClickUpService clickUpService;
    private final SupabasePayloadMapper payloadMapper;
    private final LeadToClickUpTaskMapper taskMapper;

    @Transactional
    public Object processSupabaseWebhook(SupabaseWebhookPayload payload) {
        log.info("Procesando webhook Supabase: tabla={} tipo={}",
            payload.getTable(), payload.getType());

        if (!"leads".equals(payload.getTable())) {
            log.debug("Ignorado: no es tabla leads");
            return null;
        }

        if (!clickUpService.isConfigured()) {
            log.warn("ClickUp no configurado. Se omite operación.");
            return null;
        }

        try {
            switch (payload.getType()) {
                case "INSERT":
                    return handleInsert(payload);
                case "DELETE":
                    return handleDelete(payload);
                default:
                    log.debug("Tipo de operación no soportado: {}", payload.getType());
                    return null;
            }
        } catch (Exception e) {
            log.error("Error al procesar webhook Supabase", e);
            throw e;
        }
    }

    private ClickUpTaskResponse handleInsert(SupabaseWebhookPayload payload) {
        log.info("Procesando INSERT de lead");
        
        LeadPayloadDto dto = payloadMapper.toDto(payload);
        return clickUpService.createTask(taskMapper.toClickUpTask(dto));
    }

    private Boolean handleDelete(SupabaseWebhookPayload payload) {
        log.info("Procesando DELETE de lead");
        
        LeadPayloadDto dto = payloadMapper.toDto(payload);
        
        if (dto.getLeadNumber() == null || dto.getLeadNumber().trim().isEmpty()) {
            log.warn("No se puede eliminar: lead_number no disponible en el payload");
            return false;
        }
        
        // Buscar y eliminar la tarea por lead_number usando custom fields
        boolean deleted = clickUpService.deleteTaskByLeadNumber(dto.getLeadNumber());
        
        if (deleted) {
            log.info("Lead y tarea eliminados exitosamente: leadNumber={}", dto.getLeadNumber());
        } else {
            log.warn("No se pudo eliminar la tarea para leadNumber={}", dto.getLeadNumber());
        }

        return deleted;
    }
}
