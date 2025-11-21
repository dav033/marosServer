package io.dav033.maroconstruction.dto;

public class LeadPayloadDto {
    private Integer id;
    private String leadNumber;
    private String name;
    private String leadType;
    private String location;
    private String startDate;
    private String status;
    private Long contactId;

    public LeadPayloadDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLeadNumber() { return leadNumber; }
    public void setLeadNumber(String leadNumber) { this.leadNumber = leadNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLeadType() { return leadType; }
    public void setLeadType(String leadType) { this.leadType = leadType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final LeadPayloadDto instance = new LeadPayloadDto();

        public Builder id(Integer id) { instance.setId(id); return this; }
        public Builder leadNumber(String leadNumber) { instance.setLeadNumber(leadNumber); return this; }
        public Builder name(String name) { instance.setName(name); return this; }
        public Builder leadType(String leadType) { instance.setLeadType(leadType); return this; }
        public Builder location(String location) { instance.setLocation(location); return this; }
        public Builder startDate(String startDate) { instance.setStartDate(startDate); return this; }
        public Builder status(String status) { instance.setStatus(status); return this; }
        public Builder contactId(Long contactId) { instance.setContactId(contactId); return this; }
        public LeadPayloadDto build() { return instance; }
    }
}
