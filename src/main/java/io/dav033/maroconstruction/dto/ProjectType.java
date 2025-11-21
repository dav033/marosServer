package io.dav033.maroconstruction.dto;

public class ProjectType {
    private Long id;
    private String name;
    private String color;

    public ProjectType() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public static Builder builder() { return new Builder(); }
    public static final class Builder {
        private final ProjectType instance = new ProjectType();
        public Builder id(Long id) { instance.setId(id); return this; }
        public Builder name(String name) { instance.setName(name); return this; }
        public Builder color(String color) { instance.setColor(color); return this; }
        public ProjectType build() { return instance; }
    }
}
