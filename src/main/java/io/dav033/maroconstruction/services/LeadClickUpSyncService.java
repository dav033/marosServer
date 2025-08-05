package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.dto.LeadPayloadDto;
import io.dav033.maroconstruction.dto.Leads;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.mappers.LeadToClickUpTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio especializado para la sincronización de leads con ClickUp.
 * Desacopla la lógica de ClickUp de otros servicios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeadClickUpSyncService {

    private final ClickUpService clickUpService;
    private final LeadToClickUpTaskMapper taskMapper;

    /**
     * Sincroniza un lead con ClickUp después de una actualización.
     * Si no existe una tarea, la crea. Si existe, la actualiza.
     */
    public void syncLeadUpdate(Leads lead) {
        if (!clickUpService.isConfigured()) {
            log.debug("ClickUp no configurado: se omite sincronización para lead {}", lead.getId());
            return;
        }

        try {
            LeadPayloadDto payload = buildPayloadFromLead(lead);
            String taskId = clickUpService.findTaskIdByLeadNumber(lead.getLeadNumber());
            
            if (taskId == null) {
                // Si no existe la tarea, la creamos
                createTask(payload);
            } else {
                // Si existe, la actualizamos
                updateTask(taskId, payload);
            }
        } catch (Exception ex) {
            log.error("Error sincronizando lead {} con ClickUp: {}", lead.getId(), ex.getMessage(), ex);
        }
    }

    /**
     * Sincroniza la creación de un lead con ClickUp.
     * Se llama directamente desde LeadsService después de crear un lead.
     */
    public ClickUpTaskResponse syncLeadCreate(Leads lead) {
        if (!clickUpService.isConfigured()) {
            log.debug("ClickUp no configurado: se omite creación para lead {}", lead.getId());
            return null;
        }

        try {
            LeadPayloadDto payload = buildPayloadFromLead(lead);
            return createTask(payload);
        } catch (Exception ex) {
            log.error("Error sincronizando creación de lead {} con ClickUp: {}", lead.getId(), ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Sincroniza la eliminación de un lead con ClickUp.
     * Se llama directamente desde LeadsService antes de eliminar un lead.
     */
    public boolean syncLeadDelete(Leads lead) {
        if (!clickUpService.isConfigured()) {
            log.debug("ClickUp no configurado: se omite eliminación para lead {}", lead.getId());
            return false;
        }

        try {
            return deleteTaskByLeadNumber(lead.getLeadNumber());
        } catch (Exception ex) {
            log.error("Error sincronizando eliminación de lead {} con ClickUp: {}", lead.getId(), ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Crea una nueva tarea en ClickUp para un lead.
     */
    public ClickUpTaskResponse createTask(LeadPayloadDto payload) {
        if (!clickUpService.isConfigured()) {
            log.debug("ClickUp no configurado: se omite creación de tarea para lead {}", payload.getLeadNumber());
            return null;
        }

        if (payload.getLeadNumber() == null || payload.getLeadNumber().isBlank()) {
            log.warn("Creación de tarea ClickUp ignorada: lead_number vacío");
            return null;
        }

        try {
            ClickUpTaskRequest request = taskMapper.toClickUpTask(payload);
            ClickUpTaskResponse response = clickUpService.createTask(request);

            log.info("Tarea ClickUp creada → taskId={} leadNumber={}",
                    Optional.ofNullable(response).map(ClickUpTaskResponse::getId).orElse("n/a"),
                    payload.getLeadNumber());
            return response;
        } catch (Exception ex) {
            log.error("Error creando tarea ClickUp para lead {}: {}", payload.getLeadNumber(), ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Actualiza una tarea existente en ClickUp.
     */
    public ClickUpTaskResponse updateTask(String taskId, LeadPayloadDto payload) {
        if (!clickUpService.isConfigured()) {
            log.debug("ClickUp no configurado: se omite actualización de tarea {}", taskId);
            return null;
        }

        if (payload.getLeadNumber() == null || payload.getLeadNumber().isBlank()) {
            log.warn("Actualización de tarea ClickUp ignorada: lead_number vacío");
            return null;
        }

        try {
            ClickUpTaskRequest request = taskMapper.toClickUpTask(payload);
            ClickUpTaskResponse response = clickUpService.updateTask(taskId, request);

            log.info("Tarea ClickUp actualizada → taskId={} leadNumber={}", taskId, payload.getLeadNumber());
            return response;
        } catch (Exception ex) {
            log.error("Error actualizando tarea ClickUp {}: {}", taskId, ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Elimina una tarea de ClickUp por número de lead.
     */
    public boolean deleteTaskByLeadNumber(String leadNumber) {
        if (!clickUpService.isConfigured()) {
            log.debug("ClickUp no configurado: se omite eliminación para lead {}", leadNumber);
            return false;
        }

        if (leadNumber == null || leadNumber.isBlank()) {
            log.warn("Eliminación de tarea ClickUp ignorada: lead_number vacío");
            return false;
        }

        try {
            boolean deleted = clickUpService.deleteTaskByLeadNumber(leadNumber);
            log.info("Tarea ClickUp {} para leadNumber={}",
                    deleted ? "eliminada" : "no encontrada", leadNumber);
            return deleted;
        } catch (Exception ex) {
            log.error("Error eliminando tarea ClickUp para lead {}: {}", leadNumber, ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Construye un LeadPayloadDto a partir de un objeto Leads.
     */
    private LeadPayloadDto buildPayloadFromLead(Leads lead) {
        return LeadPayloadDto.builder()
                .leadNumber(lead.getLeadNumber())
                .name(lead.getName())
                .location(lead.getLocation())
                .startDate(Optional.ofNullable(lead.getStartDate()).map(Object::toString).orElse(null))
                .leadType(Optional.ofNullable(lead.getLeadType()).map(Enum::name).orElse(null))
                .contactId(Optional.ofNullable(lead.getContact()).map(Contacts::getId).orElse(null))
                .build();
    }
}
