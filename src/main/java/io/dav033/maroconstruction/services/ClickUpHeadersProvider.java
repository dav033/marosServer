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
            // ClickUp requiere el formato exacto del header de autorización
            headers.set("Authorization", "Bearer " + token);
            log.debug("Authorization header configurado para ClickUp");
        } else {
            log.warn("No se encontró token de acceso para ClickUp (config.clickup.access-token)");
        }
        return headers;
    }
}
