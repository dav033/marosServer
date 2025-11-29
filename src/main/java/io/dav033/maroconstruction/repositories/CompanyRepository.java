package io.dav033.maroconstruction.repositories;

import io.dav033.maroconstruction.models.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<CompanyEntity> findByCustomerTrue();
    List<CompanyEntity> findByClientTrue();
}
