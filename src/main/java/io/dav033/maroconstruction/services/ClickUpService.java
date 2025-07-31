package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.config.ClickUpConfig;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.exceptions.ClickUpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio para interactuar con la API de ClickUp
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClickUpService {

    private final ClickUpConfig clickUpConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Crea una nueva tarea en ClickUp
     * 
     * @param taskRequest Datos de la tarea a crear
     * @return Respuesta de ClickUp con los datos de la tarea creada
     * @throws ClickUpException Si hay error en la creación
     */
    public ClickUpTaskResponse createTask(ClickUpTaskRequest taskRequest) {
        try {
            String url = String.format("%s/list/%s/task",
                    clickUpConfig.getApiUrl(),
                    clickUpConfig.getListId());

            HttpHeaders headers = createHeaders();
            HttpEntity<ClickUpTaskRequest> entity = new HttpEntity<>(taskRequest, headers);

            log.info("🚀 Creando tarea en ClickUp: {}", taskRequest.getName());
            log.info("📋 Custom fields en request: {}", 
                taskRequest.getCustomFields() != null ? taskRequest.getCustomFields().size() : 0);
            if (taskRequest.getCustomFields() != null && !taskRequest.getCustomFields().isEmpty()) {
                taskRequest.getCustomFields().forEach(field -> 
                    log.info("   • Field ID: {}, Value: {}", field.getId(), field.getValue()));
            }
            log.debug("🌐 URL: {}", url);
            log.debug("📦 Payload completo: {}", taskRequest);

            ResponseEntity<ClickUpTaskResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ClickUpTaskResponse.class);

            ClickUpTaskResponse taskResponse = response.getBody();
            log.info("Tarea creada exitosamente en ClickUp. ID: {}, URL: {}",
                    taskResponse.getId(), taskResponse.getUrl());

            return taskResponse;

        } catch (HttpClientErrorException e) {
            log.error("Error del cliente al crear tarea en ClickUp: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ClickUpException(
                    "Error del cliente al crear tarea en ClickUp: " + e.getMessage(),
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("Error del servidor de ClickUp: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ClickUpException(
                    "Error del servidor de ClickUp: " + e.getMessage(),
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error inesperado al crear tarea en ClickUp", e);
            throw new ClickUpException("Error inesperado al crear tarea en ClickUp: " + e.getMessage(), e);
        }
    }

    /**
     * Crea los headers necesarios para las llamadas a ClickUp
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Usar token de acceso personal si está disponible
        if (clickUpConfig.getAccessToken() != null && !clickUpConfig.getAccessToken().isEmpty()) {
            headers.set("Authorization", clickUpConfig.getAccessToken());
        } else {
            log.warn("No se encontró token de acceso para ClickUp. Configurar clickup.access-token");
        }

        return headers;
    }

    /**
     * Verifica si la configuración de ClickUp está completa
     */
    public boolean isConfigured() {
        return clickUpConfig.getListId() != null &&
                !clickUpConfig.getListId().isEmpty() &&
                (clickUpConfig.getAccessToken() != null &&
                        !clickUpConfig.getAccessToken().isEmpty());
    }
}
