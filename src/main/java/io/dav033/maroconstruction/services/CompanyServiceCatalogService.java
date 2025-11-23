package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.CompanyService;
import io.dav033.maroconstruction.exceptions.ValidationException;
import io.dav033.maroconstruction.mappers.CompanyServiceMapper;
import io.dav033.maroconstruction.models.CompanyServiceEntity;
import io.dav033.maroconstruction.repositories.CompanyServiceRepository;
import io.dav033.maroconstruction.services.base.BaseService;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceCatalogService
        extends BaseService<CompanyService, Long, CompanyServiceEntity, CompanyServiceRepository> {

    public CompanyServiceCatalogService(CompanyServiceRepository repository,
                                        CompanyServiceMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public CompanyService create(CompanyService dto) {
        if (dto.getName() != null && repository.existsByNameIgnoreCase(dto.getName())) {
            throw new ValidationException("Company service name already exists: %s", dto.getName());
        }
        return super.create(dto);
    }

    @Override
    public CompanyService update(Long id, CompanyService dto) {
        if (dto.getName() != null) {
            CompanyServiceEntity current = repository.findById(id)
                    .orElseThrow(() -> new io.dav033.maroconstruction.exceptions.ResourceNotFoundException("Company service not found"));
            if (!dto.getName().equalsIgnoreCase(current.getName())
                    && repository.existsByNameIgnoreCase(dto.getName())) {
                throw new ValidationException("Company service name already exists: %s", dto.getName());
            }
        }
        return super.update(id, dto);
    }
}
