package io.dav033.maroconstruction.dto;

import io.dav033.maroconstruction.enums.LeadStatus;
import io.dav033.maroconstruction.enums.LeadType;

import java.time.LocalDate;

public class Leads {
    private Long id;
    private String leadNumber;
    private String name;
    private LocalDate startDate;
    private String location;
    private LeadStatus status;
    private Contacts contact;
    private ProjectType projectType;
    private LeadType leadType;

    public Leads() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLeadNumber() { return leadNumber; }
    public void setLeadNumber(String leadNumber) { this.leadNumber = leadNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status; }
    public Contacts getContact() { return contact; }
    public void setContact(Contacts contact) { this.contact = contact; }
    public ProjectType getProjectType() { return projectType; }
    public void setProjectType(ProjectType projectType) { this.projectType = projectType; }
    public LeadType getLeadType() { return leadType; }
    public void setLeadType(LeadType leadType) { this.leadType = leadType; }
}
