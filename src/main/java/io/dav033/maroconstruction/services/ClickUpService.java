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
        var fields = route.getFields();
        var fieldId = fields != null ? fields.getLeadNumberId() : null;
        return listTasks(type).stream()
            .filter(t -> t.getCustomFields() != null)
            .filter(t -> fieldId != null && t.getCustomFields().stream()
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

    public void updateTask(String taskId, ClickUpTaskRequest request) {
        java.net.URI uri = java.net.URI.create(urlBuilder.buildUpdateTaskUrl(taskId));
        org.springframework.http.HttpEntity<ClickUpTaskRequest> entity = new org.springframework.http.HttpEntity<>(request, headersProvider.get());

        org.springframework.http.ResponseEntity<String> res = exchangeWithRetry(uri, org.springframework.http.HttpMethod.PUT, entity);
        ensure2xx("PUT", uri, res);

        // Actualizar custom fields si aplica
        if (request.getCustomFields() != null && !request.getCustomFields().isEmpty()) {
            request.getCustomFields().forEach(field -> {
                java.net.URI furi = java.net.URI.create(urlBuilder.buildUpdateCustomFieldsUrl(taskId) + "/" + field.getId());
                org.springframework.http.HttpEntity<java.util.Map<String,Object>> fe = new org.springframework.http.HttpEntity<>(java.util.Map.of("value", field.getValue()), headersProvider.get());
                org.springframework.http.ResponseEntity<String> fres = exchangeWithRetry(furi, org.springframework.http.HttpMethod.POST, fe);
                ensure2xx("POST", furi, fres);
            });
        }
    }

    // -------- Helpers --------
    private org.springframework.http.ResponseEntity<String> exchangeWithRetry(java.net.URI uri, org.springframework.http.HttpMethod method, org.springframework.http.HttpEntity<?> entity) {
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                return restTemplate.exchange(uri, method, entity, String.class);
            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                int status = e.getStatusCode().value();
                if ((status == 429 || status >= 500) && attempts < 3) {
                    long backoffMs = attempts * 500L;
                    log.warn("ClickUp {} {} -> {}. Reintentando en {}ms ({} de 2). Body: {}",
                             method, uri, status, backoffMs, attempts, safe(e.getResponseBodyAsString()));
                    sleep(backoffMs);
                    continue;
                }
                log.error("ClickUp {} {} fallo definitivo ({}): {}", method, uri, status, safe(e.getResponseBodyAsString()));
                throw e;
            } catch (org.springframework.web.client.ResourceAccessException io) {
                if (attempts < 3) {
                    long backoffMs = attempts * 500L;
                    log.warn("ClickUp {} {} I/O error. Reintento en {}ms ({} de 2): {}", method, uri, backoffMs, attempts, io.getMessage());
                    sleep(backoffMs);
                    continue;
                }
                throw io;
            }
        }
    }

    private void ensure2xx(String method, java.net.URI uri, org.springframework.http.ResponseEntity<String> res) {
        if (!res.getStatusCode().is2xxSuccessful()) {
            log.error("ClickUp {} {} -> {} Body: {}", method, uri, res.getStatusCode(), safe(res.getBody()));
            throw new IllegalStateException("ClickUp: respuesta no exitosa " + res.getStatusCode());
        } else {
            log.debug("ClickUp {} {} -> {}", method, uri, res.getStatusCode());
        }
    }

    private static String safe(String s) { return s == null ? "" : s.substring(0, Math.min(400, s.length())); }
    private static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }

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
        boolean urlOk = urlBuilder.isConfigured();
        boolean routingOk = false;
        try {
            // Verifica que haya al menos un tipo ruteado correctamente
            routingOk = routingService != null && routingService.route(io.dav033.maroconstruction.enums.LeadType.values()[0]) != null;
        } catch (Exception e) {
            routingOk = false;
        }
        return urlOk && routingOk;
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

    /**
     * Busca el taskId de un leadNumber en todas las listas configuradas (todos los LeadType).
     * Devuelve el primer taskId encontrado, o null si no existe en ninguna lista.
     */
    /** Busca el taskId por leadNumber iterando SOLO tipos configurados. */
    public java.util.Optional<String> findTaskIdByLeadNumberInAnyList(String leadNumber) {
        for (LeadType t : routingService.configuredTypes()) {
            try {
                String taskId = findTaskIdByLeadNumber(t, leadNumber);
                if (taskId != null) return java.util.Optional.of(taskId);
            } catch (IllegalStateException ex) {
                log.warn("Ruta incompleta para {}: {}. Continúo con otros tipos.", t, ex.getMessage());
            }
        }
        return java.util.Optional.empty();
    }

    /** True si el LeadType tiene ruteo configurado. */
    public boolean isTypeConfigured(LeadType type) {
        return routingService.isConfigured(type);
    }

    /** Borra por leadNumber dentro de un LeadType (si existe). */
}