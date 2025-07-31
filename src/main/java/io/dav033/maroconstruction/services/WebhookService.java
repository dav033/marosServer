package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.config.ClickUpConfig;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;
import io.dav033.maroconstruction.mappers.LeadToClickUpTaskMapper;
import io.dav033.maroconstruction.mappers.SupabasePayloadMapper;
import io.dav033.maroconstruction.dto.LeadPayloadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final ClickUpService clickUpService;
    private final ClickUpConfig  clickUpConfig;
    private final SupabasePayloadMapper payloadMapper;
    private final LeadToClickUpTaskMapper taskMapper;

    public ClickUpTaskResponse processSupabaseWebhook(SupabaseWebhookPayload payload) {
        log.info("Procesando webhook Supabase: tabla={} tipo={}",
            payload.getTable(), payload.getType());

        if (!"INSERT".equals(payload.getType()) || !"leads".equals(payload.getTable())) {
            log.debug("Ignorado: no es INSERT en tabla leads");
            return null;
        }
        if (!clickUpService.isConfigured()) {
            log.warn("ClickUp no configurado. Se omite creación de tarea.");
            return null;
        }

        LeadPayloadDto dto = payloadMapper.toDto(payload);
        try {
            return clickUpService.createTask(
                taskMapper.toClickUpTask(dto)
            );
        } catch (Exception e) {
            log.error("Error al crear tarea en ClickUp", e);
            throw e;
        }
    }
}
