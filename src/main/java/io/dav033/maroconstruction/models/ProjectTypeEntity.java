package io.dav033.maroconstruction.models;

import jakarta.persistence.*;

@Entity
@Table(name = "project_type")
public class ProjectTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

        @Column(name = "name", columnDefinition = "text")
    private String name;

    @Column(name ="color", columnDefinition = "text")
    private String color;

    public ProjectTypeEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
