package io.dav033.maroconstruction.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getDateCreated() { return dateCreated; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
    public String getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(String dateUpdated) { this.dateUpdated = dateUpdated; }

    public static class Status {
        private String status;
        private String color;
        private Integer orderindex;
        private String type;
        public Status() {}
        public Status(String status, String color, Integer orderindex, String type) {
            this.status = status; this.color = color; this.orderindex = orderindex; this.type = type;
        }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public Integer getOrderindex() { return orderindex; }
        public void setOrderindex(Integer orderindex) { this.orderindex = orderindex; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
