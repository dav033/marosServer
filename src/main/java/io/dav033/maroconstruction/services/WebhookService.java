package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;
import io.dav033.maroconstruction.mappers.LeadToClickUpTaskMapper;
import io.dav033.maroconstruction.mappers.SupabasePayloadMapper;
import io.dav033.maroconstruction.dto.LeadPayloadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio consolidado para el procesamiento de webhooks de Supabase.
 * Maneja todas las operaciones CRUD de leads y su sincronización con ClickUp.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final ClickUpService clickUpService;
    private final SupabasePayloadMapper payloadMapper;
    private final LeadToClickUpTaskMapper taskMapper;

    /*------------------------------------------------------------
     *  Dispatcher principal
     *-----------------------------------------------------------*/
    @Transactional
    public Object processSupabaseWebhook(SupabaseWebhookPayload payload) {
        log.info("Supabase webhook recibido: tabla={} tipo={}", payload.getTable(), payload.getType());

        if (!"leads".equalsIgnoreCase(payload.getTable()))
            return null; // 🎯 Guard clause
        if (!clickUpService.isConfigured()) {
            log.warn("ClickUp no configurado: se omite procesamiento");
            return null;
        }

        return switch (payload.getType()) {
            case "INSERT" -> handleInsert(payload);
            case "UPDATE" -> handleUpdate(payload);
            case "DELETE" -> handleDelete(payload);
            default -> {
                log.debug("Tipo de operación no soportado: {}", payload.getType());
                yield null;
            }
        };
    }

    /*------------------------------------------------------------
     *  Métodos públicos específicos para cada operación
     *-----------------------------------------------------------*/
    
    @Transactional
    public ClickUpTaskResponse processLeadInsert(SupabaseWebhookPayload payload) {
        log.info("Procesando INSERT de lead: tabla={}", payload.getTable());
        return handleInsert(payload);
    }
    
    @Transactional
    public ClickUpTaskResponse processLeadUpdate(SupabaseWebhookPayload payload) {
        log.info("Procesando UPDATE de lead: tabla={}", payload.getTable());
        return handleUpdate(payload);
    }
    
    @Transactional
    public Boolean processLeadDelete(SupabaseWebhookPayload payload) {
        log.info("Procesando DELETE de lead: tabla={}", payload.getTable());
        return handleDelete(payload);
    }

    /*------------------------------------------------------------
     *  INSERT
     *-----------------------------------------------------------*/
    private ClickUpTaskResponse handleInsert(SupabaseWebhookPayload payload) {
        LeadPayloadDto dto = payloadMapper.toDto(payload);
        if (dto.getLeadNumber() == null || dto.getLeadNumber().isBlank()) {
            log.warn("INSERT ignorado: lead_number vacío");
            return null;
        }
        ClickUpTaskRequest request = taskMapper.toClickUpTask(dto);
        ClickUpTaskResponse response = clickUpService.createTask(request);

        log.info("ClickUp task creada → taskId={} leadNumber={}",
                Optional.ofNullable(response).map(ClickUpTaskResponse::getId).orElse("n/a"),
                dto.getLeadNumber());
        return response;
    }

    /*------------------------------------------------------------
     *  UPDATE
     *-----------------------------------------------------------*/
    private ClickUpTaskResponse handleUpdate(SupabaseWebhookPayload payload) {
        LeadPayloadDto dto = payloadMapper.toDto(payload);
        if (dto.getLeadNumber() == null || dto.getLeadNumber().isBlank()) {
            log.warn("UPDATE ignorado: lead_number vacío");
            return null;
        }

        String taskId = clickUpService.findTaskIdByLeadNumber(dto.getLeadNumber());
        if (taskId == null) {
            log.warn("No se encontró tarea ClickUp para leadNumber={}", dto.getLeadNumber());
            return null;
        }

        ClickUpTaskRequest request = taskMapper.toClickUpTask(dto);
        ClickUpTaskResponse response = clickUpService.updateTask(taskId, request);

        log.info("ClickUp task actualizada → taskId={} leadNumber={}", taskId, dto.getLeadNumber());
        return response;
    }

    /*------------------------------------------------------------
     *  DELETE
     *-----------------------------------------------------------*/
    private boolean handleDelete(SupabaseWebhookPayload payload) {
        LeadPayloadDto dto = payloadMapper.toDto(payload);
        if (dto.getLeadNumber() == null || dto.getLeadNumber().isBlank()) {
            log.warn("DELETE ignorado: lead_number vacío");
            return false;
        }

        boolean deleted = clickUpService.deleteTaskByLeadNumber(dto.getLeadNumber());
        log.info("ClickUp task {} para leadNumber={}",
                deleted ? "eliminada" : "no encontrada", dto.getLeadNumber());
        return deleted;
    }
}