package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.config.ClickUpConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClickUpHeadersProvider {

    private final ClickUpConfig config;

    public ClickUpHeadersProvider(ClickUpConfig config) {
        this.config = config;
    }

    public HttpHeaders get() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String token = config.getAccessToken();
        if (token != null && !token.isBlank()) {
            // Para Personal Access Tokens (pk_), ClickUp NO requiere "Bearer "
            // Solo para OAuth tokens se requiere "Bearer "
            headers.set("Authorization", token);
            log.info("Token configurado para ClickUp: {}", token.substring(0, Math.min(10, token.length())) + "...");
        } else {
            log.warn("No se encontró token de acceso para ClickUp (config.clickup.access-token)");
        }
        return headers;
    }
}
