package io.dav033.maroconstruction.models;

import jakarta.persistence.*;

@Entity
@Table(name = "company_services")
public class CompanyServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    public CompanyServiceEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
