package io.dav033.maroconstruction.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * DTO para crear tareas en ClickUp
 * Basado en la API de ClickUp v2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickUpTaskRequest {
    
    /**
     * Nombre de la tarea (requerido)
     */
    private String name;
    
    /**
     * Descripción de la tarea
     */
    private String description;
    
    /**
     * Asignados (array de user_ids)
     */
    private List<Integer> assignees;
    
    /**
     * Tags de la tarea
     */
    private List<String> tags;
    
    /**
     * Estado de la tarea
     */
    private String status;
    
    /**
     * Prioridad: 1 = Urgent, 2 = High, 3 = Normal, 4 = Low
     */
    private Integer priority;
    
    /**
     * Fecha de vencimiento (timestamp en ms)
     */
    @JsonProperty("due_date")
    private Long dueDate;
    
    /**
     * Fecha de inicio (timestamp en ms)
     */
    @JsonProperty("start_date")
    private Long startDate;
    
    /**
     * Tiempo estimado en ms
     */
    @JsonProperty("time_estimate")
    private Long timeEstimate;
    
    /**
     * Campos personalizados
     */
    @JsonProperty("custom_fields")
    private List<CustomField> customFields;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomField {
        private String id;
        private Object value;
    }
}
