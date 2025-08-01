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
                log.warn("Cannot create task: lead_number not available in payload");
                return null;
            }
            
            ClickUpTaskResponse response = clickUpService.createTask(taskMapper.toClickUpTask(dto));
            
            if (response != null) {
                log.info("Task created successfully in ClickUp: leadNumber={}, taskId={}", 
                        dto.getLeadNumber(), response.getId());
            } else {
                log.warn("Could not create task in ClickUp for leadNumber={}", dto.getLeadNumber());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error al procesar INSERT de lead", e);
            throw e;
        }
    }
}
