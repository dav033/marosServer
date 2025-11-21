package io.dav033.maroconstruction.dto;

import java.time.LocalDateTime;

public class Contacts {
    private Long id;
    private String companyName;
    private String name;
    private String occupation;
    private String product;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime lastContact;

    public Contacts() {}

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
}
