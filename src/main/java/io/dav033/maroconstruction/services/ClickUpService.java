package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickUpService {

    private final ClickUpUrlBuilder urlBuilder;
    private final ClickUpHeadersProvider headersProvider;
    private final RestTemplate restTemplate;

    public ClickUpTaskResponse createTask(ClickUpTaskRequest taskRequest) {
        String url = urlBuilder.buildCreateTaskUrl();
        HttpEntity<ClickUpTaskRequest> entity = new HttpEntity<>(taskRequest, headersProvider.get());

        log.info("Creando tarea en ClickUp: {}", taskRequest.getName());
        logCustomFields(taskRequest.getCustomFields());

        ClickUpTaskResponse response = restTemplate.postForObject(url, entity, ClickUpTaskResponse.class);

        log.info(
                "Tarea creada con éxito en ClickUp → id={}, url={}",
                response.getId(), response.getUrl());
        return response;
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