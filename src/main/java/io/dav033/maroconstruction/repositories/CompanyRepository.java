package io.dav033.maroconstruction.repositories;

import io.dav033.maroconstruction.models.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    boolean existsByNameIgnoreCase(String name);
}
