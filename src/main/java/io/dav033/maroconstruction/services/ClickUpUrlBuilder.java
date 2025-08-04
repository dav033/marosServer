package io.dav033.maroconstruction.services;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.stream.Stream;

import io.dav033.maroconstruction.config.ClickUpConfig;

@Service
public class ClickUpUrlBuilder {

    private final ClickUpConfig config;

    public ClickUpUrlBuilder(ClickUpConfig config) {
        this.config = config;
    }

    public String buildCreateTaskUrl() {
        return buildUrl("list", config.getListId(), "task");
    }

    public String buildGetTasksUrl() {
        return buildCreateTaskUrl();
    }

    public String buildDeleteTaskUrl(String taskId) {
        return buildUrl("task", taskId);
    }

    public String buildUpdateTaskUrl(String taskId) {
        return buildDeleteTaskUrl(taskId);
    }

    public String buildUpdateCustomFieldsUrl(String taskId) {
        return buildUrl("task", taskId, "field");
    }

    public boolean isConfigured() {
        return Stream.of(
                config.getApiUrl(),
                config.getListId(),
                config.getAccessToken()
            )
            .allMatch(s -> s != null && !s.isBlank());
    }

    /**
     * Construye la URL base a partir de config.getApiUrl()
     * y añade tantos segmentos de ruta como se especifiquen.
     * Se usa fromUriString(...) en lugar del obsoleto fromHttpUrl(...).
     */
    private String buildUrl(String... segments) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(config.getApiUrl().trim());  // método moderno, no obsoleto
        for (String segment : segments) {
            builder.pathSegment(segment.trim());
        }
        return builder.toUriString();
    }
}
