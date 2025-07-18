package io.dav033.maroconstruction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("Authorization", "Content-Type", "Accept", "X-Requested-With", "remember-me",
                                "Access-Control-Allow-Origin", "Access-Control-Allow-Headers", "Origin",
                                "Access-Control-Request-Method", "Access-Control-Request-Headers")
                        .exposedHeaders("Authorization", "Content-Type", "Accept", "X-Requested-With", "remember-me",
                                "Access-Control-Allow-Origin", "Access-Control-Allow-Headers", "Origin",
                                "Access-Control-Request-Method", "Access-Control-Request-Headers");
            }
        };
    }
}