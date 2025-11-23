package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.Company;
import io.dav033.maroconstruction.enums.CompanyType;
import io.dav033.maroconstruction.exceptions.ResourceNotFoundException;
import io.dav033.maroconstruction.exceptions.ValidationException;
import io.dav033.maroconstruction.mappers.CompanyMapper;
import io.dav033.maroconstruction.models.CompanyEntity;
import io.dav033.maroconstruction.models.CompanyServiceEntity;
import io.dav033.maroconstruction.repositories.CompanyRepository;
import io.dav033.maroconstruction.repositories.CompanyServiceRepository;
import io.dav033.maroconstruction.services.base.BaseService;
import org.springframework.stereotype.Service;

@Service
public class CompanyService
        extends BaseService<Company, Long, CompanyEntity, CompanyRepository> {

    private final CompanyServiceRepository companyServiceRepository;

    public CompanyService(CompanyRepository repository,
                          CompanyMapper mapper,
                          CompanyServiceRepository companyServiceRepository) {
        super(repository, mapper);
        this.companyServiceRepository = companyServiceRepository;
    }

    @Override
    public Company create(Company dto) {
        validate(dto, null);
        CompanyEntity entity = mapper.toEntity(dto);
        applyService(dto, entity);
        CompanyEntity saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public Company update(Long id, Company dto) {
        CompanyEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        validate(dto, id);
        mapper.updateEntity(dto, entity);
        applyService(dto, entity);
        CompanyEntity saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    private void validate(Company dto, Long id) {
        if (dto.getName() != null) {
            boolean exists = (id == null)
                    ? repository.existsByNameIgnoreCase(dto.getName())
                    : repository.existsByNameIgnoreCase(dto.getName()) && !repository.findById(id)
                        .map(e -> e.getName().equalsIgnoreCase(dto.getName()))
                        .orElse(false);
            if (exists) {
                throw new ValidationException("Company name already exists: %s", dto.getName());
            }
        }

        if (dto.getType() == CompanyType.CONTRACTOR && dto.getServiceId() == null) {
            throw new ValidationException("Contractor companies must have a serviceId defined");
        }
    }

    private void applyService(Company dto, CompanyEntity entity) {
        if (dto.getServiceId() == null) {
            entity.setService(null);
            return;
        }
        CompanyServiceEntity serviceEntity = companyServiceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Company service not found"));
        entity.setService(serviceEntity);
    }
}
