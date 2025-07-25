package io.dav033.maroconstruction.repositories;

import io.dav033.maroconstruction.enums.LeadType;
import io.dav033.maroconstruction.models.LeadsEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LeadsRepository extends JpaRepository<LeadsEntity, Long> {

    @EntityGraph(attributePaths = {"projectType", "contact"})
    List<LeadsEntity> findByLeadType(LeadType type);
}
