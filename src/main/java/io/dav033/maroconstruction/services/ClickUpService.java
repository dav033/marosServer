package io.dav033.maroconstruction.services;

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
            String url = urlBuilder.buildUpdateTaskUrl(taskId);
            HttpEntity<ClickUpTaskRequest> entity = new HttpEntity<>(taskRequest, headersProvider.get());

            log.info("Actualizando tarea en ClickUp: taskId={}, name={}", taskId, taskRequest.getName());
            logCustomFields(taskRequest.getCustomFields());

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

            log.info("Tarea actualizada con éxito en ClickUp → id={}, url={}", 
                    responseBody.getId(), responseBody.getUrl());
            return responseBody;
            
        } catch (RestClientException e) {
            log.error("Error al comunicarse con ClickUp API para actualizar tarea: {}", e.getMessage(), e);
            throw new ClickUpException("Error updating task in ClickUp: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al actualizar tarea en ClickUp: {}", e.getMessage(), e);
            throw new ClickUpException("Unexpected error updating task in ClickUp: " + e.getMessage(), e);
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
        log.debug("Custom fields count: {}", count);
        if (count > 0) {
            fields.forEach(f -> log.debug(" • Field id={}, value={}", f.getId(), f.getValue()));
        }
    }
}