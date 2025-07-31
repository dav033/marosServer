package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
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
        // Validar configuración antes de proceder
        if (!isConfigured()) {
            throw new ClickUpException("ClickUp no está configurado correctamente. Revisa las propiedades de configuración.");
        }
        
        // Validar que el request no esté vacío
        if (taskRequest == null || taskRequest.getName() == null || taskRequest.getName().trim().isEmpty()) {
            throw new ClickUpException("El request de tarea es inválido: nombre de tarea es requerido");
        }
        
        try {
            String url = urlBuilder.buildCreateTaskUrl();
            HttpEntity<ClickUpTaskRequest> entity = new HttpEntity<>(taskRequest, headersProvider.get());

            log.info("Creando tarea en ClickUp: {}", taskRequest.getName());
            logCustomFields(taskRequest.getCustomFields());

            ClickUpTaskResponse response = restTemplate.postForObject(url, entity, ClickUpTaskResponse.class);

            if (response == null) {
                throw new ClickUpException("La respuesta de ClickUp fue nula");
            }

            log.info(
                    "Tarea creada con éxito en ClickUp → id={}, url={}",
                    response.getId(), response.getUrl());
            return response;
            
        } catch (RestClientException e) {
            log.error("Error al comunicarse con ClickUp API: {}", e.getMessage(), e);
            throw new ClickUpException("Error al crear tarea en ClickUp: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al crear tarea en ClickUp: {}", e.getMessage(), e);
            throw new ClickUpException("Error inesperado al crear tarea en ClickUp: " + e.getMessage(), e);
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