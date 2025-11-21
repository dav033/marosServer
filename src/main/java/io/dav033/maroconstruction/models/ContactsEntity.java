package io.dav033.maroconstruction.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "company_name", length = 100, nullable = false)
    private String companyName;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "product", length = 100)
    private String product;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 255)
    private String address;
    @Column(name = "last_contact")
    private LocalDateTime lastContact;

    public ContactsEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDateTime getLastContact() { return lastContact; }
    public void setLastContact(LocalDateTime lastContact) { this.lastContact = lastContact; }

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
                ", companyName='" + companyName + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
