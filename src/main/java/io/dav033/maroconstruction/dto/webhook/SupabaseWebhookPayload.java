package io.dav033.maroconstruction.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * DTO para recibir el payload del webhook de Supabase
 * Según la documentación de Supabase Database Webhooks
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupabaseWebhookPayload {
    
    /**
     * Tipo de evento: INSERT, UPDATE, DELETE
     */
    private String type;
    
    /**
     * Tabla afectada
     */
    private String table;
    
    /**
     * Esquema de la base de datos
     */
    private String schema;
    
    /**
     * Datos antiguos (para UPDATE y DELETE)
     */
    @JsonProperty("old_record")
    private Map<String, Object> oldRecord;
    
    /**
     * Datos nuevos (para INSERT y UPDATE)
     */
    private Map<String, Object> record;
    
    /**
     * Columnas que cambiaron (para UPDATE)
     */
    private String[] columns;
}
