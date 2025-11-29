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

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "is_customer", nullable = false)
    private boolean customer = false;

    @Column(name = "is_client", nullable = false)
    private boolean client = false;

    @Column(name = "notes", columnDefinition = "text")
    @Convert(converter = StringListToJsonConverter.class)
    private java.util.List<String> notes;

    public CompanyEntity() {}

    public java.util.List<String> getNotes() { return notes; }
    public void setNotes(java.util.List<String> notes) { this.notes = notes; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public CompanyType getType() { return type; }
    public void setType(CompanyType type) { this.type = type; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public boolean isCustomer() { return customer; }
    public void setCustomer(boolean customer) { this.customer = customer; }
    public boolean isClient() { return client; }
    public void setClient(boolean client) { this.client = client; }
}
