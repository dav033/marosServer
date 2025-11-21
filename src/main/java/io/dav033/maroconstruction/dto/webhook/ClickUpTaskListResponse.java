package io.dav033.maroconstruction.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ClickUpTaskListResponse {

    private List<ClickUpTaskSummary> tasks;

    public List<ClickUpTaskSummary> getTasks() { return tasks; }
    public void setTasks(List<ClickUpTaskSummary> tasks) { this.tasks = tasks; }

    public static class ClickUpTaskSummary {
        private String id;
        private String name;
        private String description;
        private String url;

        @JsonProperty("custom_fields")
        private List<CustomFieldValue> customFields;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public List<CustomFieldValue> getCustomFields() { return customFields; }
        public void setCustomFields(List<CustomFieldValue> customFields) { this.customFields = customFields; }

        public static class CustomFieldValue {
            private String id;
            private String name;
            private String type;
            private Object value;
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public Object getValue() { return value; }
            public void setValue(Object value) { this.value = value; }
        }
    }
}
