package io.dav033.maroconstruction.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "clickup")
public class ClickUpConfig {
    
    private String apiUrl = "https://api.clickup.com/api/v2";
    private String clientId;
    private String clientSecret;
    private String accessToken;
    private String teamId;
    private String spaceId;
    private String listId;
    private Integer defaultPriority = 3;
}
