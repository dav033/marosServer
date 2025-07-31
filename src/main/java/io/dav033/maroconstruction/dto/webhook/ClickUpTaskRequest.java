package io.dav033.maroconstruction.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClickUpTaskRequest {
    
    private String name;
    private String description;
    private List<Integer> assignees;
    private List<String> tags;
    private String status;
    private Integer priority;
    
    @JsonProperty("due_date")
    private Long dueDate;
    
    @JsonProperty("start_date")
    private Long startDate;
    
    @JsonProperty("time_estimate")
    private Long timeEstimate;
    
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
