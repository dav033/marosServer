package io.dav033.maroconstruction.repositories;

import io.dav033.maroconstruction.enums.ProjectStatus;
import io.dav033.maroconstruction.models.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

  List<ProjectEntity> findByProjectStatus(ProjectStatus status);

  @Query("""
    SELECT p FROM ProjectEntity p
      JOIN FETCH p.lead l
      LEFT JOIN FETCH l.contact c
    WHERE p.lead IS NOT NULL
  """)
  List<ProjectEntity> findProjectsWithLeadAndContact();

  @Query("SELECT COUNT(p) FROM ProjectEntity p WHERE p.lead IS NOT NULL")
  Long countProjectsWithLead();
}
