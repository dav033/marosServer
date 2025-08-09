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

        String token = config.getAccessToken(); // Debe venir como 'pk_xxx'
        if (token == null || token.isBlank()) {
            log.warn("ClickUp: access token no configurado");
        } else {
            // Si alguien puso "Bearer pk_...", límpielo para evitar 401.
            if (token.startsWith("Bearer ")) {
                token = token.substring("Bearer ".length());
            }
            headers.set("Authorization", token);
        }
        return headers;
    }
    /**
     * @deprecated Usar solo accessToken (Bearer ...) para autenticación.
     */
    @Deprecated
    public String getClientSecret() {
        return config.getClientSecret();
    }
}
