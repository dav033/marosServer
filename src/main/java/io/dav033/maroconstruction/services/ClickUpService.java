package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.config.ClickUpConfig;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskListResponse;
import io.dav033.maroconstruction.exceptions.ClickUpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickUpService {

    private final ClickUpUrlBuilder urlBuilder;
    private final ClickUpHeadersProvider headersProvider;
    private final RestTemplate restTemplate;
    private final ClickUpConfig config;

    public ClickUpTaskResponse createTask(ClickUpTaskRequest taskRequest) {
        if (!isConfigured()) {
            throw new ClickUpException("ClickUp is not configured correctly. Check configuration properties.");
        }
        
        if (taskRequest == null || taskRequest.getName() == null || taskRequest.getName().trim().isEmpty()) {
            throw new ClickUpException("Task request is invalid: task name is required");
        }
        
        try {
            String url = urlBuilder.buildCreateTaskUrl();
            HttpEntity<ClickUpTaskRequest> entity = new HttpEntity<>(taskRequest, headersProvider.get());

            log.info("Creando tarea en ClickUp: {}", taskRequest.getName());
            logCustomFields(taskRequest.getCustomFields());

            ClickUpTaskResponse response = restTemplate.postForObject(url, entity, ClickUpTaskResponse.class);

            if (response == null) {
                throw new ClickUpException("ClickUp response was null");
            }

            log.info(
                    "Tarea creada con éxito en ClickUp → id={}, url={}",
                    response.getId(), response.getUrl());
            return response;
            
        } catch (RestClientException e) {
            log.error("Error al comunicarse con ClickUp API: {}", e.getMessage(), e);
            throw new ClickUpException("Error creating task in ClickUp: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al crear tarea en ClickUp: {}", e.getMessage(), e);
            throw new ClickUpException("Unexpected error creating task in ClickUp: " + e.getMessage(), e);
        }
    }

    public boolean deleteTask(String taskId) {
        if (!isConfigured()) {
            throw new ClickUpException("ClickUp is not configured correctly. Check configuration properties.");
        }
        
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new ClickUpException("Task ID is required for deletion");
        }
        
        try {
            String url = urlBuilder.buildDeleteTaskUrl(taskId);
            HttpHeaders headers = headersProvider.get();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Eliminando tarea en ClickUp: {}", taskId);

            // Usar exchange en lugar de delete para poder pasar headers
            ResponseEntity<Void> response = restTemplate.exchange(
                url, 
                HttpMethod.DELETE, 
                entity, 
                Void.class
            );

            log.info("Tarea eliminada con éxito en ClickUp → id={}, status={}", taskId, response.getStatusCode());
            return true;
            
        } catch (RestClientException e) {
            log.error("Error al comunicarse con ClickUp API para eliminar tarea: {}", e.getMessage(), e);
            throw new ClickUpException("Error deleting task in ClickUp: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al eliminar tarea en ClickUp: {}", e.getMessage(), e);
            throw new ClickUpException("Unexpected error deleting task in ClickUp: " + e.getMessage(), e);
        }
    }

    public ClickUpTaskResponse updateTask(String taskId, ClickUpTaskRequest taskRequest) {
        if (!isConfigured()) {
            throw new ClickUpException("ClickUp is not configured correctly. Check configuration properties.");
        }
        
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new ClickUpException("Task ID is required for update");
        }
        
        if (taskRequest == null || taskRequest.getName() == null || taskRequest.getName().trim().isEmpty()) {
            throw new ClickUpException("Task request is invalid: task name is required");
        }
        
        try {
            // Paso 1: Actualizar los campos básicos de la tarea (sin custom fields)
            ClickUpTaskRequest basicRequest = ClickUpTaskRequest.builder()
                .name(taskRequest.getName())
                .description(taskRequest.getDescription())
                .tags(taskRequest.getTags())
                .priority(taskRequest.getPriority())
                .status(taskRequest.getStatus())
                .startDate(taskRequest.getStartDate())
                .dueDate(taskRequest.getDueDate())
                .timeEstimate(taskRequest.getTimeEstimate())
                .assignees(taskRequest.getAssignees())
                // No incluir custom fields en esta request
                .build();
            
            String url = urlBuilder.buildUpdateTaskUrl(taskId);
            HttpEntity<ClickUpTaskRequest> entity = new HttpEntity<>(basicRequest, headersProvider.get());

            log.info("Actualizando tarea básica en ClickUp: taskId={}, name={}", taskId, taskRequest.getName());

            // ClickUp utiliza PUT para actualizar tareas
            ResponseEntity<ClickUpTaskResponse> response = restTemplate.exchange(
                url, 
                HttpMethod.PUT, 
                entity, 
                ClickUpTaskResponse.class
            );

            ClickUpTaskResponse responseBody = response.getBody();
            if (responseBody == null) {
                throw new ClickUpException("ClickUp update response was null");
            }

            log.info("Tarea básica actualizada con éxito en ClickUp → id={}", responseBody.getId());
            
            // Paso 2: Actualizar los custom fields por separado si existen
            if (taskRequest.getCustomFields() != null && !taskRequest.getCustomFields().isEmpty()) {
                updateCustomFields(taskId, taskRequest.getCustomFields());
            } else {
                log.info("No custom fields to update for task {}", taskId);
            }
            
            return responseBody;
            
        } catch (RestClientException e) {
            log.error("Error al comunicarse con ClickUp API para actualizar tarea: {}", e.getMessage(), e);
            throw new ClickUpException("Error updating task in ClickUp: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al actualizar tarea en ClickUp: {}", e.getMessage(), e);
            throw new ClickUpException("Unexpected error updating task in ClickUp: " + e.getMessage(), e);
        }
    }
    
    private void updateCustomFields(String taskId, List<ClickUpTaskRequest.CustomField> customFields) {
        try {
            log.info("Actualizando {} custom fields para tarea {}", customFields.size(), taskId);
            logCustomFields(customFields);
            
            // Actualizar cada custom field individualmente
            for (ClickUpTaskRequest.CustomField field : customFields) {
                updateSingleCustomField(taskId, field);
            }
            
            log.info("Todos los custom fields actualizados exitosamente para tarea {}", taskId);
            
        } catch (Exception e) {
            log.error("Error actualizando custom fields para tarea {}: {}", taskId, e.getMessage(), e);
            // No lanzar excepción para no fallar toda la actualización por un custom field
        }
    }
    
    private void updateSingleCustomField(String taskId, ClickUpTaskRequest.CustomField field) {
        try {
            String url = String.format("%s/task/%s/field/%s", 
                config.getApiUrl(), taskId, field.getId());
            
            // El body para actualizar un custom field específico
            var fieldUpdateRequest = new Object() {
                @SuppressWarnings("unused")
                public final Object value = field.getValue();
            };
            
            HttpEntity<Object> entity = new HttpEntity<>(fieldUpdateRequest, headersProvider.get());
            
            log.debug("Actualizando custom field: taskId={}, fieldId={}, value='{}'", 
                    taskId, field.getId(), field.getValue());
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, // ClickUp usa POST para actualizar custom fields
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Custom field {} actualizado exitosamente", field.getId());
            } else {
                log.warn("Respuesta no exitosa al actualizar custom field {}: {}", 
                        field.getId(), response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error actualizando custom field {} para tarea {}: {}", 
                    field.getId(), taskId, e.getMessage());
        }
    }

    public boolean deleteTaskByLeadNumber(String leadNumber) {
        log.info("Buscando tarea para eliminar por lead_number: {}", leadNumber);
        
        String taskId = findTaskIdByLeadNumber(leadNumber);
        if (taskId != null) {
            return deleteTask(taskId);
        } else {
            log.warn("No se encontró tarea con lead_number: {}", leadNumber);
            return false;
        }
    }

    public String findTaskIdByLeadNumber(String leadNumber) {
        if (!isConfigured()) {
            throw new ClickUpException("ClickUp is not configured correctly.");
        }
        
        try {
            String url = urlBuilder.buildGetTasksUrl();
            HttpEntity<Void> entity = new HttpEntity<>(headersProvider.get());

            log.info("Obteniendo tareas de ClickUp para buscar lead_number: {}", leadNumber);

            ClickUpTaskListResponse response = restTemplate.exchange(
                url, HttpMethod.GET, entity, ClickUpTaskListResponse.class
            ).getBody();

            if (response != null && response.getTasks() != null) {
                for (ClickUpTaskListResponse.ClickUpTaskSummary task : response.getTasks()) {
                    if (task.getCustomFields() != null) {
                        for (ClickUpTaskListResponse.ClickUpTaskSummary.CustomFieldValue field : task.getCustomFields()) {
                            if ("53d6e312-0f63-40ba-8f87-1f3092d8b322".equals(field.getId()) && 
                                leadNumber.equals(String.valueOf(field.getValue()))) {
                                
                                log.info("Encontrada tarea con lead_number {}: taskId={}", leadNumber, task.getId());
                                return task.getId();
                            }
                        }
                    }
                }
            }
            
            log.warn("No se encontró ninguna tarea con lead_number: {}", leadNumber);
            return null;
            
        } catch (RestClientException e) {
            log.error("Error al buscar tareas en ClickUp: {}", e.getMessage(), e);
            throw new ClickUpException("Error searching tasks in ClickUp: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al buscar tareas: {}", e.getMessage(), e);
            throw new ClickUpException("Unexpected error searching tasks: " + e.getMessage(), e);
        }
    }

    public boolean isConfigured() {
        return urlBuilder.isConfigured();
    }

    private void logCustomFields(List<ClickUpTaskRequest.CustomField> fields) {
        int count = (fields == null ? 0 : fields.size());
        log.info("Custom fields being sent to ClickUp: {} fields", count);
        if (count > 0) {
            fields.forEach(f -> log.info(" • Field id={}, value='{}'", f.getId(), f.getValue()));
        } else {
            log.warn("No custom fields found in request - contact info may not be updated in ClickUp");
        }
    }
}