package io.dav033.maroconstruction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClickUpClientConfig {

    @Bean
    public WebClient clickUpWebClient(ClickUpProperties props) {
        return WebClient.builder()
            .baseUrl("https://api.clickup.com/api/v2")
            .defaultHeader("Authorization", props.getClientSecret())
            .build();
    }
}
