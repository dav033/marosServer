package io.dav033.maroconstruction.repositories;

import io.dav033.maroconstruction.models.LeadsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadsRepository extends JpaRepository<LeadsEntity, Long> {
    // Additional query methods can be defined here if needed
}
