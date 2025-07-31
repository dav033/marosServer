package io.dav033.maroconstruction.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClickUpTaskResponse {
    
    private String id;
    private String name;
    private String description;
    private Status status;
    private String url;
    
    @JsonProperty("date_created")
    private String dateCreated;
    
    @JsonProperty("date_updated")
    private String dateUpdated;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private String status;
        private String color;
        private Integer orderindex;
        private String type;
    }
}
