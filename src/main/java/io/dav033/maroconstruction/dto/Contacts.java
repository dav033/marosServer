package io.dav033.maroconstruction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Contacts {
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
    private Long id;
    private String name;
    private String occupation;
    private String phone;
    private String email;
    private String address;
    
    @JsonProperty("isCustomer")
    private boolean customer;

    @JsonProperty("isClient")
    private boolean client;

    @JsonProperty("companyId")
    private Long companyId;

    public Contacts() {}

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

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
}
