package io.dav033.maroconstruction.repositories;

import io.dav033.maroconstruction.models.LeadClickUpMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadClickUpMappingRepository extends JpaRepository<LeadClickUpMapping, Long> {
    
    Optional<LeadClickUpMapping> findByLeadId(Long leadId);
    
    Optional<LeadClickUpMapping> findByLeadNumber(String leadNumber);
    
    Optional<LeadClickUpMapping> findByClickUpTaskId(String clickUpTaskId);
    
    void deleteByLeadId(Long leadId);
    
    void deleteByClickUpTaskId(String clickUpTaskId);
}
