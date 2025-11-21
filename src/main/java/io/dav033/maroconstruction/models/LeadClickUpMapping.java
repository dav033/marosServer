package io.dav033.maroconstruction.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_clickup_mapping")
public class LeadClickUpMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "lead_id", nullable = false, unique = true)
    private Long leadId;
    
    @Column(name = "lead_number", nullable = false)
    private String leadNumber;
    
    @Column(name = "clickup_task_id", nullable = false)
    private String clickUpTaskId;
    
    @Column(name = "clickup_task_url")
    private String clickUpTaskUrl;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public LeadClickUpMapping() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }
    public String getLeadNumber() { return leadNumber; }
    public void setLeadNumber(String leadNumber) { this.leadNumber = leadNumber; }
    public String getClickUpTaskId() { return clickUpTaskId; }
    public void setClickUpTaskId(String clickUpTaskId) { this.clickUpTaskId = clickUpTaskId; }
    public String getClickUpTaskUrl() { return clickUpTaskUrl; }
    public void setClickUpTaskUrl(String clickUpTaskUrl) { this.clickUpTaskUrl = clickUpTaskUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
