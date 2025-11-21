package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.config.ClickUpConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ClickUpHeadersProvider {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClickUpHeadersProvider.class);

    private final ClickUpConfig config;

    public ClickUpHeadersProvider(ClickUpConfig config) {
        this.config = config;
    }

    public HttpHeaders get() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String token = config.getAccessToken(); 
        if (token == null || token.isBlank()) {
            log.warn("ClickUp: access token no configurado");
        } else {
            if (token.startsWith("Bearer ")) {
                token = token.substring("Bearer ".length());
            }
            headers.set("Authorization", token);
        }
        return headers;
    }
        @Deprecated
    public String getClientSecret() {
        return config.getClientSecret();
    }
}
