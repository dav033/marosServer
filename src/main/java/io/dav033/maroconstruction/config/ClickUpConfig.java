package io.dav033.maroconstruction.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para la integración con ClickUp
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "clickup")
public class ClickUpConfig {
    
    /**
     * URL base de la API de ClickUp
     */
    private String apiUrl = "https://api.clickup.com/api/v2";
    
    /**
     * Client ID de ClickUp
     */
    private String clientId;
    
    /**
     * Client Secret de ClickUp
     */
    private String clientSecret;
    
    /**
     * Token de acceso personal (si no usas OAuth)
     */
    private String accessToken;
    
    /**
     * ID del workspace/team
     */
    private String teamId;
    
    /**
     * ID del space donde crear las tareas
     */
    private String spaceId;
    
    /**
     * ID de la lista donde crear las tareas
     */
    private String listId;
    
    /**
     * Estado por defecto para nuevas tareas
     */
    private String defaultStatus = "to do";
    
    /**
     * Prioridad por defecto: 1 = Urgent, 2 = High, 3 = Normal, 4 = Low
     */
    private Integer defaultPriority = 3;
}
