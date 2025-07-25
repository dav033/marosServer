package io.dav033.maroconstruction.repositories;

import io.dav033.maroconstruction.models.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

}
