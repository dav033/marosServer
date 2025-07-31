package io.dav033.maroconstruction.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClickUpTaskListResponse {
    
    private List<ClickUpTaskSummary> tasks;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClickUpTaskSummary {
        private String id;
        private String name;
        private String description;
        private String url;
        
        @JsonProperty("custom_fields")
        private List<CustomFieldValue> customFields;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CustomFieldValue {
            private String id;
            private String name;
            private String type;
            private Object value;
        }
    }
}
