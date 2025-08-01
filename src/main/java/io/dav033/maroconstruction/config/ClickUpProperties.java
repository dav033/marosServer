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
    private String clientId;
    private String clientSecret;
}
