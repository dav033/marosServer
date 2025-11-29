package io.dav033.maroconstruction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dav033.maroconstruction.enums.CompanyType;

public class Company {
    private Long id;
    private String name;
    private String address;
    private CompanyType type;
    private Long serviceId;

    @JsonProperty("isCustomer")
    private boolean customer;

    @JsonProperty("isClient")
    private boolean client;

    private java.util.List<String> notes;

    public Company() {}

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
