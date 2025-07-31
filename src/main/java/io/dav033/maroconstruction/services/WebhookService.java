package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;
import io.dav033.maroconstruction.mappers.LeadToClickUpTaskMapper;
import io.dav033.maroconstruction.mappers.SupabasePayloadMapper;
import io.dav033.maroconstruction.models.LeadClickUpMapping;
import io.dav033.maroconstruction.repositories.LeadClickUpMappingRepository;
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
    private final LeadClickUpMappingRepository mappingRepository;
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
        ClickUpTaskResponse taskResponse = clickUpService.createTask(
            taskMapper.toClickUpTask(dto)
        );

        // Guardar el mapping entre lead y tarea de ClickUp
        if (taskResponse != null && dto.getId() != null) {
            LeadClickUpMapping mapping = LeadClickUpMapping.builder()
                .leadId(dto.getId().longValue())
                .leadNumber(dto.getLeadNumber())
                .clickUpTaskId(taskResponse.getId())
                .clickUpTaskUrl(taskResponse.getUrl())
                .build();
            
            mappingRepository.save(mapping);
            log.info("Mapping guardado: leadId={} → clickUpTaskId={}", 
                dto.getId(), taskResponse.getId());
        }

        return taskResponse;
    }

    private Boolean handleDelete(SupabaseWebhookPayload payload) {
        log.info("Procesando DELETE de lead");
        
        LeadPayloadDto dto = payloadMapper.toDto(payload);
        
        // Buscar el mapping para obtener el ID de la tarea en ClickUp
        var mappingOpt = dto.getId() != null 
            ? mappingRepository.findByLeadId(dto.getId().longValue())
            : mappingRepository.findByLeadNumber(dto.getLeadNumber());
            
        if (mappingOpt.isEmpty()) {
            log.warn("No se encontró mapping para lead ID={} o leadNumber={}", 
                dto.getId(), dto.getLeadNumber());
            return false;
        }

        LeadClickUpMapping mapping = mappingOpt.get();
        
        // Eliminar la tarea en ClickUp
        boolean deleted = clickUpService.deleteTask(mapping.getClickUpTaskId());
        
        if (deleted) {
            // Eliminar el mapping de la base de datos
            mappingRepository.delete(mapping);
            log.info("Lead y tarea eliminados: leadId={} → clickUpTaskId={}", 
                mapping.getLeadId(), mapping.getClickUpTaskId());
        }

        return deleted;
    }
}
