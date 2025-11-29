package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.Company;
import io.dav033.maroconstruction.exceptions.ResourceNotFoundException;
import io.dav033.maroconstruction.exceptions.ValidationException;
import io.dav033.maroconstruction.mappers.CompanyMapper;
import io.dav033.maroconstruction.models.CompanyEntity;
import io.dav033.maroconstruction.models.ContactsEntity;
import io.dav033.maroconstruction.repositories.CompanyRepository;
import io.dav033.maroconstruction.repositories.ContactsRepository;
import io.dav033.maroconstruction.services.base.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CompanyService
        extends BaseService<Company, Long, CompanyEntity, CompanyRepository> {

    private final ContactsRepository contactsRepository;

    public CompanyService(CompanyRepository repository, CompanyMapper mapper, ContactsRepository contactsRepository) {
        super(repository, mapper);
        this.contactsRepository = contactsRepository;
    }

    @Override
    public Company create(Company dto) {
        validate(dto, null);
        CompanyEntity entity = mapper.toEntity(dto);
        CompanyEntity saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public Company update(Long id, Company dto) {
        CompanyEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        validate(dto, id);
        mapper.updateEntity(dto, entity);
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
    }

    public List<Company> findCustomers() {
        List<CompanyEntity> entities = repository.findByCustomerTrue();
        return mapper.toDtoList(entities);
    }

    public List<Company> findClients() {
        List<CompanyEntity> entities = repository.findByClientTrue();
        return mapper.toDtoList(entities);
    }

    @Transactional
    public void assignContactsToCompany(Long companyId, List<Long> contactIds) {
        CompanyEntity company = repository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        // Remove company from contacts that are no longer selected
        List<ContactsEntity> currentContacts = contactsRepository.findByCompanyId(companyId);
        for (ContactsEntity contact : currentContacts) {
            if (!contactIds.contains(contact.getId())) {
                contact.setCompany(null);
                contactsRepository.save(contact);
            }
        }
        
        // Assign company to selected contacts
        for (Long contactId : contactIds) {
            ContactsEntity contact = contactsRepository.findById(contactId)
                    .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + contactId));
            contact.setCompany(company);
            contactsRepository.save(contact);
        }
    }
}
