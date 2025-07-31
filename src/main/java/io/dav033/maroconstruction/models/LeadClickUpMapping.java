package io.dav033.maroconstruction.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_clickup_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
