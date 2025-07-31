package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;
import io.dav033.maroconstruction.mappers.SupabasePayloadMapper;
import io.dav033.maroconstruction.dto.LeadPayloadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadDeleteService {

    private final ClickUpService clickUpService;
    private final SupabasePayloadMapper payloadMapper;

    @Transactional
    public Boolean processLeadDelete(SupabaseWebhookPayload payload) {
        log.info("Procesando DELETE de lead");
        
        if (!clickUpService.isConfigured()) {
            log.warn("ClickUp no configurado. Se omite eliminación de tarea.");
            return false;
        }
        
        try {
            LeadPayloadDto dto = payloadMapper.toDto(payload);
            
            if (dto.getLeadNumber() == null || dto.getLeadNumber().trim().isEmpty()) {
                log.warn("No se puede eliminar: lead_number no disponible en el payload");
                return false;
            }
            
            // Buscar y eliminar la tarea por lead_number usando custom fields
            boolean deleted = clickUpService.deleteTaskByLeadNumber(dto.getLeadNumber());
            
            if (deleted) {
                log.info("Tarea eliminada exitosamente de ClickUp: leadNumber={}", dto.getLeadNumber());
            } else {
                log.warn("No se pudo eliminar la tarea de ClickUp para leadNumber={}", dto.getLeadNumber());
            }

            return deleted;
            
        } catch (Exception e) {
            log.error("Error al procesar DELETE de lead", e);
            throw e;
        }
    }
}
