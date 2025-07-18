package io.dav033.maroconstruction.repositories;

import io.dav033.maroconstruction.models.ProjectTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTypeRepository extends JpaRepository<ProjectTypeEntity, Long> {
    // Additional query methods can be defined here if needed
}
