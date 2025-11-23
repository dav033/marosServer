package io.dav033.maroconstruction.repositories;

import io.dav033.maroconstruction.models.CompanyServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyServiceRepository extends JpaRepository<CompanyServiceEntity, Long> {
    boolean existsByNameIgnoreCase(String name);
}
