package io.dav033.maroconstruction.models;

import io.dav033.maroconstruction.enums.CompanyType;
import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CompanyType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private CompanyServiceEntity service;

    @Column(name = "is_customer", nullable = false)
    private boolean isCustomer = false;

    public CompanyEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public CompanyType getType() { return type; }
    public void setType(CompanyType type) { this.type = type; }

    public CompanyServiceEntity getService() { return service; }
    public void setService(CompanyServiceEntity service) { this.service = service; }

    public boolean isCustomer() { return isCustomer; }
    public void setCustomer(boolean customer) { isCustomer = customer; }
}
