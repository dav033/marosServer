package io.dav033.maroconstruction.models;

import io.dav033.maroconstruction.enums.LeadStatus;
import io.dav033.maroconstruction.enums.LeadType;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "leads")
public class LeadsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "lead_number", length = 50, nullable = false, unique = true)
    private String leadNumber;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "location", length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "text")
    private LeadStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name ="lead_type", columnDefinition = "text")
    private LeadType leadType;

        @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "contact_id", nullable = false)
    private ContactsEntity contact;

        @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "type", nullable = false)
    private ProjectTypeEntity projectType;

    @Transient
    public Long getContactId() {
        return (contact != null ? contact.getId() : null);
    }

    @Transient
    public Long getProjectTypeId() {
        return (projectType != null ? projectType.getId() : null);
    }

    public LeadsEntity() {}

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
    public LeadType getLeadType() { return leadType; }
    public void setLeadType(LeadType leadType) { this.leadType = leadType; }
    public ContactsEntity getContact() { return contact; }
    public void setContact(ContactsEntity contact) { this.contact = contact; }
    public ProjectTypeEntity getProjectType() { return projectType; }
    public void setProjectType(ProjectTypeEntity projectType) { this.projectType = projectType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeadsEntity that = (LeadsEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LeadsEntity{" +
                "id=" + id +
                ", leadNumber='" + leadNumber + '\'' +
                ", name='" + name + '\'' +
                ", startDate=" + startDate +
                ", location='" + location + '\'' +
                '}';
    }
}
