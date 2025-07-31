package io.dav033.maroconstruction.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupabaseWebhookPayload {
    
    private String type;
    private String table;
    private String schema;
    
    @JsonProperty("old_record")
    private Map<String, Object> oldRecord;
    
    private Map<String, Object> record;
    private String[] columns;
}
