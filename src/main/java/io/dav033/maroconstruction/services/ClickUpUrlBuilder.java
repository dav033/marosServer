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
        return config.getApiUrl() != null && !config.getApiUrl().isBlank()
            && config.getAccessToken() != null && !config.getAccessToken().isBlank();
    }

        public String listFields(String listId) {
        return buildUrl("list", listId, "field");
    }

    public java.net.URI taskById(String id) {
        return java.net.URI.create(config.getApiUrl() + "/task/" + id);
    }

    public java.net.URI taskField(String id, String fieldId) {
        return java.net.URI.create(config.getApiUrl() + "/task/" + id + "/field/" + fieldId);
    }


    public String buildUrl(String... segments) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(config.getApiUrl().trim());  
        for (String segment : segments) {
            builder.pathSegment(segment.trim());
        }
        return builder.toUriString();
    }
}
