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
public class LeadInsertService {

    private final ClickUpService clickUpService;
    private final SupabasePayloadMapper payloadMapper;
    private final LeadToClickUpTaskMapper taskMapper;

    @Transactional
    public ClickUpTaskResponse processLeadInsert(SupabaseWebhookPayload payload) {
        log.info("Procesando INSERT de lead");
        
        if (!clickUpService.isConfigured()) {
            log.warn("ClickUp no configurado. Se omite creación de tarea.");
            return null;
        }
        
        try {
            LeadPayloadDto dto = payloadMapper.toDto(payload);
            
            if (dto.getLeadNumber() == null || dto.getLeadNumber().trim().isEmpty()) {
                log.warn("No se puede crear tarea: lead_number no disponible en el payload");
                return null;
            }
            
            ClickUpTaskResponse response = clickUpService.createTask(taskMapper.toClickUpTask(dto));
            
            if (response != null) {
                log.info("Tarea creada exitosamente en ClickUp: leadNumber={}, taskId={}", 
                        dto.getLeadNumber(), response.getId());
            } else {
                log.warn("No se pudo crear la tarea en ClickUp para leadNumber={}", dto.getLeadNumber());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error al procesar INSERT de lead", e);
            throw e;
        }
    }
}
