

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
     * Sincroniza la eliminación de un lead con ClickUp de forma robusta y diagnóstica.
     * Devuelve un objeto de resultado con status, taskId, diagnóstico y acciones.
     */
    public ClickUpDeleteResult syncLeadDelete(Leads lead) {
        ClickUpDeleteResult result = new ClickUpDeleteResult();
        result.setLeadNumber(lead.getLeadNumber());
        result.setLeadType(lead.getLeadType() != null ? lead.getLeadType().name() : null);
        if (!clickUpService.isConfigured()) {
            String msg = "ClickUp no configurado: se omite eliminación para lead " + lead.getId();
            log.warn(msg);
            result.setStatus("CONFIG_ERROR");
            result.setDiagnosis(msg);
            return result;
        }
        try {
            // 1. Validar configuración y obtener listId
            String listId = null;
            String leadNumberId = null;
            try {
                listId = clickUpService.getRoutingService().getListId(lead.getLeadType());
                leadNumberId = clickUpService.getRoutingService().resolveLeadNumberFieldId(lead.getLeadType());
            } catch (Exception e) {
                String msg = "Error de configuración ClickUp para leadType=" + lead.getLeadType() + ": " + e.getMessage();
                log.error(msg);
                result.setStatus("CONFIG_ERROR");
                result.setDiagnosis(msg);
                return result;
            }
            result.setListId(listId);
            result.setLeadNumberId(leadNumberId);

            // 2. Localizar tarea(s) por custom field (normalizando)
            var tasks = clickUpService.listTasks(lead.getLeadType());
            var normalizedTarget = normalize(lead.getLeadNumber());

            final String leadNumberIdFinal = leadNumberId;
            var matches = tasks.stream()
                .filter(t -> t.getCustomFields() != null)
                .filter(t -> t.getCustomFields().stream().anyMatch(cf ->
                        leadNumberIdFinal.equals(cf.getId()) && equalsNormalized(cf.getValue(), normalizedTarget)))
                .toList();

            // Reintento tras autodiscovery forzado si vacío
            if (matches.isEmpty()) {
                String leadNumberIdRetry = clickUpService.getRoutingService().resolveLeadNumberFieldId(lead.getLeadType());
                result.setLeadNumberId(leadNumberIdRetry);
                final String leadNumberIdRetryFinal = leadNumberIdRetry;
                tasks = clickUpService.listTasks(lead.getLeadType());
                matches = tasks.stream()
                    .filter(t -> t.getCustomFields() != null)
                    .filter(t -> t.getCustomFields().stream().anyMatch(cf ->
                            leadNumberIdRetryFinal.equals(cf.getId()) && equalsNormalized(cf.getValue(), normalizedTarget)))
                    .toList();
                result.setDiagnosis("Sin coincidencias tras autodiscovery. Probadas: listId=" + listId + ", fieldId=" + leadNumberIdRetry + ".");
            }

            // Buscar en otros tipos si sigue vacío
            if (matches.isEmpty()) {
                var anyList = clickUpService.findTaskIdByLeadNumberInAnyList(lead.getLeadNumber());
                if (anyList.isPresent()) {
                    result.setTaskId(anyList.get());
                    result.setStatus("FOUND_OTHER_LIST");
                    result.setDiagnosis("Lead encontrado en otro LeadType/listId. taskId=" + anyList.get());
                    // Intentar eliminar
                    return tryDeleteTask(anyList.get(), result);
                }
                String msg = "No se encontró tarea con leadNumber=" + lead.getLeadNumber() + " en listId=" + listId + " usando fieldId=" + leadNumberId;
                log.warn(msg);
                result.setStatus("NOT_FOUND");
                result.setDiagnosis(msg);
                return result;
            }

            // Desempatado: si hay más de una coincidencia
            if (matches.size() > 1) {
                log.warn("Múltiples coincidencias para leadNumber={}: {}. Se selecciona la más reciente.", lead.getLeadNumber(), matches.stream().map(t -> t.getId()).toList());
                result.setDiagnosis("Múltiples coincidencias. Tareas: " + matches.stream().map(t -> t.getId()).toList() + ". Se seleccionó la más reciente.");
            }
            // Heurística: elegir la más reciente (por defecto la primera)
            var chosen = matches.get(0);
            result.setTaskId(chosen.getId());
            return tryDeleteTask(chosen.getId(), result);
        } catch (Exception ex) {
            String msg = "Error sincronizando eliminación de lead " + lead.getId() + " con ClickUp: " + ex.getMessage();
            log.error(msg, ex);
            result.setStatus("ERROR");
            result.setDiagnosis(msg);
            return result;
        }
    }

    private ClickUpDeleteResult tryDeleteTask(String taskId, ClickUpDeleteResult result) {
        try {
            boolean deleted = clickUpService.deleteTask(taskId);
            if (deleted) {
                result.setStatus("DELETED");
                result.setDiagnosis("Eliminación completada. taskId=" + taskId);
                log.info("Eliminación completada. leadType={}, listId={}, leadNumberId={}, taskId={}",
                        result.getLeadType(), result.getListId(), result.getLeadNumberId(), taskId);
            } else {
                result.setStatus("DELETE_FAILED");
                result.setDiagnosis("No se pudo eliminar la tarea. taskId=" + taskId);
            }
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
            // 404: reintentar localización
            log.warn("DELETE 404: tarea no encontrada en ClickUp. taskId={}. Reintentando localización...", taskId);
            result.setStatus("NOT_FOUND_AFTER_DELETE");
            result.setDiagnosis("DELETE 404: tarea no encontrada en ClickUp. taskId=" + taskId);
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden | org.springframework.web.client.HttpClientErrorException.Unauthorized auth) {
            result.setStatus("AUTH_ERROR");
            result.setDiagnosis("DELETE 403/401: credenciales inválidas o sin permisos. taskId=" + taskId);
        } catch (org.springframework.web.client.HttpClientErrorException.Conflict conf) {
            result.setStatus("CONFLICT");
            result.setDiagnosis("DELETE 409: estado inconsistente en ClickUp. taskId=" + taskId);
        } catch (Exception ex) {
            result.setStatus("ERROR");
            result.setDiagnosis("Error eliminando tarea en ClickUp: " + ex.getMessage());
        }
        return result;
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private boolean equalsNormalized(Object value, String target) {
        if (value == null) return false;
        String s = (value instanceof Number) ? String.valueOf(((Number) value).longValue()) : String.valueOf(value);
        return normalize(s).equals(target);
    }

    // DTO de resultado para diagnóstico y status
    @lombok.Data
    public static class ClickUpDeleteResult {
        private String status;
        private String leadNumber;
        private String leadType;
        private String listId;
        private String leadNumberId;
        private String taskId;
        private String diagnosis;
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
