package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.config.ClickUpConfig;
import io.dav033.maroconstruction.config.ClickUpRoutingService;
import io.dav033.maroconstruction.enums.LeadType;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskListResponse;
import io.dav033.maroconstruction.exceptions.ClickUpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
    private final ClickUpRoutingService routingService;

    // Nuevo método: crear tarea por tipo
    public ClickUpTaskResponse createTask(LeadType type, ClickUpTaskRequest taskRequest) {
        validateConfigured();
        validateTaskRequest(taskRequest);
        var listId = routingService.route(type).getListId();
        return execute("create task", () -> {
            String url = urlBuilder.buildUrl("list", listId, "task");
            HttpEntity<ClickUpTaskRequest> entity = new HttpEntity<>(taskRequest, headersProvider.get());
            log.info("Creando tarea en ClickUp: {} para tipo {}", taskRequest.getName(), type);
            logCustomFields(taskRequest.getCustomFields());
            return restTemplate.postForObject(url, entity, ClickUpTaskResponse.class);
        });
    }

    // Nuevo método: listar tareas por tipo
    public List<ClickUpTaskListResponse.ClickUpTaskSummary> listTasks(LeadType type) {
        var listId = routingService.route(type).getListId();
        return execute("list tasks", () -> {
            String url = urlBuilder.buildUrl("list", listId, "task");
            HttpEntity<Void> entity = new HttpEntity<>(headersProvider.get());
            ClickUpTaskListResponse resp = restTemplate.exchange(url, HttpMethod.GET, entity, ClickUpTaskListResponse.class).getBody();
            return resp == null ? List.of() : resp.getTasks();
        });
    }

    // Nuevo método: buscar taskId por leadNumber y tipo
    public String findTaskIdByLeadNumber(LeadType type, String leadNumber) {
        var route = routingService.route(type);
        var fieldId = route.getLeadNumberId();
        return listTasks(type).stream()
            .filter(t -> t.getCustomFields() != null)
            .filter(t -> t.getCustomFields().stream()
                .anyMatch(f -> fieldId.equals(f.getId()) && leadNumber.equals(String.valueOf(f.getValue()))))
            .map(ClickUpTaskListResponse.ClickUpTaskSummary::getId)
            .findFirst().orElse(null);
    }

    // Nuevo método: eliminar por tipo y leadNumber
    public boolean deleteTaskByLeadNumber(LeadType type, String leadNumber) {
        var id = findTaskIdByLeadNumber(type, leadNumber);
        return id != null && deleteTask(id);
    }

    public boolean deleteTask(String taskId) {
        validateConfigured();
        validateTaskId(taskId);

        execute("delete task", () -> {
            String url = urlBuilder.buildDeleteTaskUrl(taskId);
            HttpEntity<Void> entity = new HttpEntity<>(headersProvider.get());
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            return null;
        });
        log.info("Tarea eliminada con éxito en ClickUp → id={}", taskId);
        return true;
    }

    public ClickUpTaskResponse updateTask(String taskId, ClickUpTaskRequest taskRequest) {
        validateConfigured();
        validateTaskId(taskId);
        validateTaskRequest(taskRequest);

        // Actualiza datos básicos
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
                .build();

        ClickUpTaskResponse response = execute("update task", () -> {
            String url = urlBuilder.buildUpdateTaskUrl(taskId);
            HttpEntity<ClickUpTaskRequest> entity = new HttpEntity<>(basicRequest, headersProvider.get());
            return restTemplate.exchange(url, HttpMethod.PUT, entity, ClickUpTaskResponse.class).getBody();
        });

        // Actualiza custom fields si existen
        if (taskRequest.getCustomFields() != null && !taskRequest.getCustomFields().isEmpty()) {
            updateCustomFields(taskId, taskRequest.getCustomFields());
        }
        return response;
    }

    public boolean deleteTaskByLeadNumber(String leadNumber) {
        log.debug("Buscando tarea para eliminar por lead_number: {}", leadNumber);
        String taskId = findTaskIdByLeadNumber(leadNumber);
        return taskId != null && deleteTask(taskId);
    }

    public String findTaskIdByLeadNumber(String leadNumber) {
        validateConfigured();
        return execute("find task by lead number", () -> {
            String url = urlBuilder.buildGetTasksUrl();
            HttpEntity<Void> entity = new HttpEntity<>(headersProvider.get());
            ClickUpTaskListResponse list = restTemplate
                    .exchange(url, HttpMethod.GET, entity, ClickUpTaskListResponse.class).getBody();
            if (list == null || list.getTasks() == null)
                return null;
            return list.getTasks().stream()
                    .filter(t -> t.getCustomFields() != null)
                    .filter(t -> t.getCustomFields().stream()
                            .anyMatch(f -> "53d6e312-0f63-40ba-8f87-1f3092d8b322".equals(f.getId())
                                    && leadNumber.equals(String.valueOf(f.getValue()))))
                    .map(ClickUpTaskListResponse.ClickUpTaskSummary::getId)
                    .findFirst()
                    .orElse(null);
        });
    }

    public boolean isConfigured() {
        return urlBuilder.isConfigured();
    }

    private void updateCustomFields(String taskId, List<ClickUpTaskRequest.CustomField> customFields) {
        customFields.forEach(field -> execute("update custom field", () -> {
            String url = String.format("%s/task/%s/field/%s", config.getApiUrl(), taskId, field.getId());
            var requestBody = Map.of("value", field.getValue());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headersProvider.get());
            restTemplate.postForEntity(url, entity, Void.class);
            return null;
        }));
    }

    private <T> T execute(String action, Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RestClientException e) {
            log.error("Error durante la acción '{}' en ClickUp: {}", action, e.getMessage(), e);
            throw new ClickUpException("ClickUp " + action + " failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado durante la acción '{}' en ClickUp: {}", action, e.getMessage(), e);
            throw new ClickUpException(
                    "Unexpected error in ClickUp while attempting to " + action + ": " + e.getMessage(), e);
        }
    }

    private void validateConfigured() {
        if (!isConfigured()) {
            throw new ClickUpException("ClickUp is not configured correctly. Check configuration properties.");
        }
    }

    private void validateTaskId(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new ClickUpException("Task ID is required");
        }
    }

    private void validateTaskRequest(ClickUpTaskRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new ClickUpException("Task request is invalid: task name is required");
        }
    }

    private void logCustomFields(List<ClickUpTaskRequest.CustomField> fields) {
        if (fields == null || fields.isEmpty()) {
            log.debug("No custom fields en la petición.");
            return;
        }
        fields.forEach(f -> log.debug("📌 Field {} → value '{}'", f.getId(), f.getValue()));
    }
}