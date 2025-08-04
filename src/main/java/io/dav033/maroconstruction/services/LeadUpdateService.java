package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
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
public class LeadUpdateService {

    private final ClickUpService clickUpService;
    private final SupabasePayloadMapper payloadMapper;
    private final LeadToClickUpTaskMapper taskMapper;

    @Transactional
    public ClickUpTaskResponse processLeadUpdate(SupabaseWebhookPayload payload) {
        log.info("Procesando UPDATE de lead");
        
        if (!clickUpService.isConfigured()) {
            log.warn("ClickUp no configurado. Se omite actualización de tarea.");
            return null;
        }
        
        try {
            LeadPayloadDto dto = payloadMapper.toDto(payload);
            
            log.info("Processing lead update: leadNumber={}, contactId={}, name={}", 
                    dto.getLeadNumber(), dto.getContactId(), dto.getName());
            
            if (dto.getLeadNumber() == null || dto.getLeadNumber().trim().isEmpty()) {
                log.warn("Cannot update task: lead_number not available in payload");
                return null;
            }
            
            // Buscar el ID de la tarea en ClickUp por lead_number
            String taskId = clickUpService.findTaskIdByLeadNumber(dto.getLeadNumber());
            
            if (taskId != null) {
                log.info("Found task to update in ClickUp: leadNumber={}, taskId={}", 
                        dto.getLeadNumber(), taskId);
                
                // Crear el request de actualización usando el mapper
                ClickUpTaskRequest updateRequest = taskMapper.toClickUpTask(dto);
                
                // Actualizar la tarea en ClickUp
                ClickUpTaskResponse response = clickUpService.updateTask(taskId, updateRequest);
                
                if (response != null) {
                    log.info("Task updated successfully in ClickUp: leadNumber={}, taskId={}", 
                            dto.getLeadNumber(), response.getId());
                } else {
                    log.warn("Could not update task in ClickUp for leadNumber={}", dto.getLeadNumber());
                }
                
                return response;
            } else {
                log.warn("No task found in ClickUp for lead_number: {}", dto.getLeadNumber());
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error al procesar UPDATE de lead", e);
            throw e;
        }
    }
}
