

package io.dav033.maroconstruction.services;
import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.dto.LeadPayloadDto;
import io.dav033.maroconstruction.dto.Leads;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.enums.LeadType;
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
            LeadPayloadDto payload = LeadPayloadDto.builder()
                .leadNumber(lead.getLeadNumber())
                .name(lead.getName())
                .location(lead.getLocation())
                .startDate(Optional.ofNullable(lead.getStartDate()).map(Object::toString).orElse(null))
                .leadType(Optional.ofNullable(lead.getLeadType()).map(Enum::name).orElse(null))
                .contactId(Optional.ofNullable(lead.getContact()).map(Contacts::getId).orElse(null))
                .build();
            ClickUpTaskRequest req = taskMapper.toClickUpTask(payload);
            // 1) Buscar en la lista del tipo actual
            String taskId = clickUpService.findTaskIdByLeadNumber(lead.getLeadType(), lead.getLeadNumber());
            if (taskId == null) {
                taskId = clickUpService.findTaskIdByLeadNumberInAnyList(lead.getLeadNumber()).orElse(null);
            }
            if (taskId != null) {
                clickUpService.updateTask(taskId, req);
                log.info("ClickUp UPDATE ok: taskId={} lead={} type={}", taskId, lead.getLeadNumber(), lead.getLeadType());
            } else {
                log.error("ClickUp UPDATE omitido: no se encontró tarea para lead={} (type={})", lead.getLeadNumber(), lead.getLeadType());
            }
        } catch (Exception ex) {
            log.error("Error sincronizando lead {} con ClickUp: {}", lead.getId(), ex.getMessage(), ex);
        }
    }


    /**
     * Sincroniza la creación de un lead con ClickUp.
     * Se llama directamente desde LeadsService después de crear un lead.
     */
    public void syncLeadCreate(Leads lead) {
        if (!clickUpService.isConfigured()) {
            log.debug("ClickUp no configurado: se omite creación para lead {}", lead.getId());
            return;
        }
        try {
            LeadPayloadDto payload = LeadPayloadDto.builder()
                .leadNumber(lead.getLeadNumber())
                .name(lead.getName())
                .location(lead.getLocation())
                .startDate(Optional.ofNullable(lead.getStartDate()).map(Object::toString).orElse(null))
                .leadType(Optional.ofNullable(lead.getLeadType()).map(Enum::name).orElse(null))
                .contactId(Optional.ofNullable(lead.getContact()).map(Contacts::getId).orElse(null))
                .build();
            ClickUpTaskRequest req = taskMapper.toClickUpTask(payload);
            ClickUpTaskResponse created = clickUpService.createTask(lead.getLeadType(), req);
            log.info("ClickUp CREATE ok: taskId={} lead={} type={}", created != null ? created.getId() : "n/a", lead.getLeadNumber(), lead.getLeadType());
        } catch (Exception ex) {
            log.error("Error sincronizando creación de lead {} con ClickUp: {}", lead.getId(), ex.getMessage(), ex);
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
            String taskId = clickUpService.findTaskIdByLeadNumber(lead.getLeadType(), lead.getLeadNumber());
            if (taskId == null) taskId = clickUpService.findTaskIdByLeadNumberInAnyList(lead.getLeadNumber()).orElse(null);
            if (taskId != null) {
                clickUpService.deleteTask(taskId);
                log.info("ClickUp DELETE ok: taskId={} lead={}", taskId, lead.getLeadNumber());
                return true;
            } else {
                log.warn("ClickUp DELETE: no se encontró tarea para lead={}", lead.getLeadNumber());
                return false;
            }
        } catch (Exception ex) {
            log.error("Error sincronizando eliminación de lead {} con ClickUp: {}", lead.getId(), ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Crea una nueva tarea en ClickUp para un lead.
     */
    // Eliminado: método createTask(...) que lanzaba UnsupportedOperationException

    /**
     * Actualiza una tarea existente en ClickUp.
     */
    // Eliminado: método updateTask(...) que lanzaba UnsupportedOperationException

    /**
     * Elimina una tarea de ClickUp por número de lead.
     */
    /**
     * @deprecated Usar clickUpService.deleteTaskByLeadNumber(LeadType, leadNumber) directamente.
     */
    @Deprecated
    public boolean deleteTaskByLeadNumber(String leadNumber) {
        throw new UnsupportedOperationException("Usar clickUpService.deleteTaskByLeadNumber(LeadType, leadNumber)");
    }

    /**
     * Construye un LeadPayloadDto a partir de un objeto Leads.
     */
}
