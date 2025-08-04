package io.dav033.maroconstruction.services;

import org.springframework.stereotype.Component;

import io.dav033.maroconstruction.config.ClickUpConfig;

@Component
public class ClickUpUrlBuilder {

    private final ClickUpConfig config;

    public ClickUpUrlBuilder(ClickUpConfig config) {
        this.config = config;
    }

    public String buildCreateTaskUrl() {
        return String.format("%s/list/%s/task",
            config.getApiUrl(),
            config.getListId());
    }

    public String buildDeleteTaskUrl(String taskId) {
        return String.format("%s/task/%s",
            config.getApiUrl(),
            taskId);
    }

    public String buildUpdateTaskUrl(String taskId) {
        return String.format("%s/task/%s",
            config.getApiUrl(),
            taskId);
    }

    public String buildUpdateCustomFieldsUrl(String taskId) {
        return String.format("%s/task/%s/field",
            config.getApiUrl(),
            taskId);
    }

    public String buildGetTasksUrl() {
        return String.format("%s/list/%s/task",
            config.getApiUrl(),
            config.getListId());
    }

    public boolean isConfigured() {
        return config.getApiUrl()   != null && !config.getApiUrl().isBlank()
            && config.getListId()   != null && !config.getListId().isBlank()
            && config.getAccessToken() != null && !config.getAccessToken().isBlank();
    }
}
