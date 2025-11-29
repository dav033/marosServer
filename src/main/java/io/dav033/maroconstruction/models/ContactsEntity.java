package io.dav033.maroconstruction.models;

import jakarta.persistence.*;

@Entity
@Table(
    name = "contacts",
    indexes = {
        @Index(name = "ux_contacts_email_ci", columnList = "email"),
        @Index(name = "ux_contacts_phone", columnList = "phone"),
        @Index(name = "ux_contacts_name_ci", columnList = "name")
    }
)
public class ContactsEntity {
    @Column(name = "notes", columnDefinition = "text")
    private String notesJson;

    public java.util.List<String> getNotes() {
        if (notesJson == null || notesJson.isEmpty()) return new java.util.ArrayList<>();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(notesJson, java.util.List.class);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    public void setNotes(java.util.List<String> notes) {
        try {
            this.notesJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(notes);
        } catch (Exception e) {
            this.notesJson = "[]";
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "is_customer", nullable = false)
    private boolean customer = false;

    @Column(name = "is_client", nullable = false)
    private boolean client = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private CompanyEntity company;

    public ContactsEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isCustomer() { return customer; }
    public void setCustomer(boolean customer) { this.customer = customer; }
    public boolean isClient() { return client; }
    public void setClient(boolean client) { this.client = client; }

    public CompanyEntity getCompany() { return company; }
    public void setCompany(CompanyEntity company) { this.company = company; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactsEntity that = (ContactsEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public String toString() {
        return "ContactsEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
