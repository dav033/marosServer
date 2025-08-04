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
            String fieldDescription = getFieldDescription(field.getId());
            log.info("🔧 ===== ACTUALIZANDO CUSTOM FIELD =====");
            log.info("🔧 Campo: {}", fieldDescription);
            log.info("🔧 Field ID: '{}'", field.getId());
            log.info("🔧 Nuevo valor: '{}'", field.getValue());
            log.info("🔧 Task ID: '{}'", taskId);
            
            String url = String.format("%s/task/%s/field/%s", 
                config.getApiUrl(), taskId, field.getId());
            
            // El body para actualizar un custom field específico
            var fieldUpdateRequest = new Object() {
                @SuppressWarnings("unused")
                public final Object value = field.getValue();
            };
            
            HttpEntity<Object> entity = new HttpEntity<>(fieldUpdateRequest, headersProvider.get());
            
            log.info("🔧 URL de actualización: {}", url);
            log.info("🔧 Request body: {}", fieldUpdateRequest);
            log.info("🔧 Enviando petición a ClickUp...");
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, // ClickUp usa POST para actualizar custom fields
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ {} ACTUALIZADO EXITOSAMENTE", fieldDescription);
                log.info("✅ Status: {}", response.getStatusCode());
                log.info("✅ Response: {}", response.getBody());
            } else {
                log.error("❌ ERROR AL ACTUALIZAR {}", fieldDescription);
                log.error("❌ Status: {}", response.getStatusCode());
                log.error("❌ Response Body: {}", response.getBody());
            }
            log.info("===== FIN ACTUALIZACIÓN CUSTOM FIELD =====");
            
        } catch (Exception e) {
            String fieldDescription = getFieldDescription(field.getId());
            log.error("💥 EXCEPCIÓN AL ACTUALIZAR {}", fieldDescription);
            log.error("💥 Field ID: '{}'", field.getId());
            log.error("💥 Task ID: '{}'", taskId);
            log.error("💥 Error: {}", e.getMessage());
            log.error("💥 Stack trace:", e);
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

    /**
     * Actualiza completamente un task de ClickUp con nueva información de contacto
     * Actualiza: nombre, descripción y todos los custom fields
     */
    public ClickUpTaskResponse updateTaskWithNewContact(String taskId, ClickUpTaskRequest newTaskData) {
        if (!isConfigured()) {
            throw new ClickUpException("ClickUp is not configured correctly. Check configuration properties.");
        }
        
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new ClickUpException("Task ID is required for update");
        }
        
        if (newTaskData == null) {
            throw new ClickUpException("New task data is required");
        }
        
        try {
            log.info("🔄 STARTING COMPLETE TASK UPDATE: taskId={}", taskId);
            
            // 📋 LOG COMPLETO DE LA REQUEST RECIBIDA
            log.info("� ===== REQUEST COMPLETA RECIBIDA =====");
            log.info("📝 Task Name: '{}'", newTaskData.getName());
            log.info("📄 Description: '{}'", newTaskData.getDescription());
            log.info("🏷️ Tags: {}", newTaskData.getTags());
            log.info("⚡ Priority: {}", newTaskData.getPriority());
            log.info("📅 Start Date: {}", newTaskData.getStartDate());
            log.info("📅 Due Date: {}", newTaskData.getDueDate());
            log.info("👥 Assignees: {}", newTaskData.getAssignees());
            log.info("🔢 Custom Fields Count: {}", 
                newTaskData.getCustomFields() != null ? newTaskData.getCustomFields().size() : 0);
            
            // 🏷️ LOG DETALLADO DE CUSTOM FIELDS
            if (newTaskData.getCustomFields() != null && !newTaskData.getCustomFields().isEmpty()) {
                log.info("🏷️ ===== CUSTOM FIELDS EN LA REQUEST =====");
                for (ClickUpTaskRequest.CustomField field : newTaskData.getCustomFields()) {
                    String fieldName = getFieldDescription(field.getId());
                    log.info("   {} → ID: '{}' → Value: '{}'", fieldName, field.getId(), field.getValue());
                }
                log.info("===== FIN CUSTOM FIELDS =====");
            } else {
                log.error("❌ NO HAY CUSTOM FIELDS EN LA REQUEST - ESTO ES UN PROBLEMA");
            }
            log.info("===== FIN REQUEST COMPLETA =====");
            
            // Paso 1: Actualizar información básica del task (nombre y descripción)
            ClickUpTaskRequest basicUpdate = ClickUpTaskRequest.builder()
                .name(newTaskData.getName())
                .description(newTaskData.getDescription())
                .build();
            
            String url = urlBuilder.buildUpdateTaskUrl(taskId);
            HttpEntity<ClickUpTaskRequest> entity = new HttpEntity<>(basicUpdate, headersProvider.get());

            log.info("🔄 Updating basic task info at URL: {}", url);
            
            ResponseEntity<ClickUpTaskResponse> response = restTemplate.exchange(
                url, 
                HttpMethod.PUT, 
                entity, 
                ClickUpTaskResponse.class
            );

            ClickUpTaskResponse responseBody = response.getBody();
            if (responseBody == null) {
                throw new ClickUpException("ClickUp basic update response was null");
            }

            log.info("✅ Basic task info updated successfully");
            
            // Paso 2: Actualizar custom fields con nueva información de contacto
            if (newTaskData.getCustomFields() != null && !newTaskData.getCustomFields().isEmpty()) {
                log.info("🔄 ===== INICIANDO ACTUALIZACIÓN DE CUSTOM FIELDS =====");
                log.info("🔢 Número de custom fields a actualizar: {}", newTaskData.getCustomFields().size());
                
                // Log de qué contacto se está usando (extraer del custom field de contact name si existe)
                newTaskData.getCustomFields().stream()
                    .filter(field -> "524a8b7c-cfb7-4361-886e-59a019f8c5b5".equals(field.getId()))
                    .findFirst()
                    .ifPresent(contactField -> 
                        log.info("🎯 ACTUALIZANDO CON INFORMACIÓN DEL CONTACTO: '{}'", contactField.getValue()));
                
                updateCustomFields(taskId, newTaskData.getCustomFields());
                log.info("✅ ===== CUSTOM FIELDS ACTUALIZADOS EXITOSAMENTE =====");
            } else {
                log.error("❌❌❌ CRÍTICO: No custom fields provided for update");
                log.error("❌ Esto significa que la información del contacto NO se actualizará en ClickUp");
                log.error("❌ Posibles causas:");
                log.error("   - CustomFieldsBuilder no encontró el contacto");
                log.error("   - Error en el mapeo de LeadToClickUpTaskMapper");
                log.error("   - ContactsService no pudo obtener el contacto por ID");
            }

            log.info("🎉 COMPLETE TASK UPDATE FINISHED: taskId={}", taskId);
            return responseBody;
            
        } catch (RestClientException e) {
            log.error("❌ Error updating complete task in ClickUp: taskId={}, error={}", taskId, e.getMessage());
            throw new ClickUpException("Failed to update complete task in ClickUp: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error updating complete task: taskId={}, error={}", taskId, e.getMessage());
            throw new ClickUpException("Unexpected error updating complete task: " + e.getMessage(), e);
        }
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

    /**
     * Devuelve una descripción legible del custom field basado en su ID
     */
    private String getFieldDescription(String fieldId) {
        switch (fieldId) {
            case "524a8b7c-cfb7-4361-886e-59a019f8c5b5": return "👤 Contact Name";
            case "c8dbf709-6ef9-479f-a915-b20518ac30e6": return "🏢 Company Name";
            case "f2220992-2039-4a6f-9717-b53ede8f5ec1": return "📧 Contact Email";
            case "9edb199d-5c9f-404f-84f1-ad6a78597175": return "📞 Contact Phone (Primary)";
            case "f94558c8-3c7a-48cb-999c-c697b7842ddf": return "📞 Contact Phone (Secondary)";
            case "401a9851-6f11-4043-b577-4c7b3f03fb03": return "📍 Location";
            case "53d6e312-0f63-40ba-8f87-1f3092d8b322": return "🔢 Lead Number";
            default: return "❓ Unknown Field";
        }
    }
}