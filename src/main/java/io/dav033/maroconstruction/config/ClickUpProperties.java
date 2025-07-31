package io.dav033.maroconstruction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "clickup")
public class ClickUpProperties {
    /**
     * Se inyecta desde CLICKUP_CLIENT_ID
     */
    private String clientId;
    /**
     * Se inyecta desde CLICKUP_CLIENT_SECRET
     */
    private String clientSecret;

    // getters y setters
}
