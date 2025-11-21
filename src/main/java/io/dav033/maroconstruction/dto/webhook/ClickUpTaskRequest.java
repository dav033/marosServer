package io.dav033.maroconstruction.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

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

    public ClickUpTaskRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Integer> getAssignees() { return assignees; }
    public void setAssignees(List<Integer> assignees) { this.assignees = assignees; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Long getDueDate() { return dueDate; }
    public void setDueDate(Long dueDate) { this.dueDate = dueDate; }
    public Long getStartDate() { return startDate; }
    public void setStartDate(Long startDate) { this.startDate = startDate; }
    public Long getTimeEstimate() { return timeEstimate; }
    public void setTimeEstimate(Long timeEstimate) { this.timeEstimate = timeEstimate; }
    public List<CustomField> getCustomFields() { return customFields; }
    public void setCustomFields(List<CustomField> customFields) { this.customFields = customFields; }

    public static class CustomField {
        private String id;
        private Object value;

        public CustomField() {}
        public CustomField(String id, Object value) {
            this.id = id; this.value = value;
        }

        public static CustomFieldBuilder builder() { return new CustomFieldBuilder(); }
        public static class CustomFieldBuilder {
            private final CustomField cf = new CustomField();
            public CustomFieldBuilder id(String v) { cf.id = v; return this; }
            public CustomFieldBuilder value(Object v) { cf.value = v; return this; }
            public CustomField build() { return cf; }
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }
}
