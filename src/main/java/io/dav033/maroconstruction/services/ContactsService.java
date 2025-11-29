package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.exceptions.ContactExceptions;
import io.dav033.maroconstruction.exceptions.ValidationException;
import io.dav033.maroconstruction.dto.responses.ContactValidationResponse;
import io.dav033.maroconstruction.mappers.ContactsMapper;
import io.dav033.maroconstruction.models.ContactsEntity;
import io.dav033.maroconstruction.models.CompanyEntity;
import io.dav033.maroconstruction.repositories.ContactsRepository;
import io.dav033.maroconstruction.repositories.CompanyRepository;
import io.dav033.maroconstruction.services.base.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ContactsService extends BaseService<Contacts, Long, ContactsEntity, ContactsRepository> {

    private final CompanyRepository companyRepository;

    public ContactsService(ContactsRepository repository, ContactsMapper contactsMapper, CompanyRepository companyRepository) {
        super(repository, contactsMapper);
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public Contacts create(Contacts dto) {
        if (dto.getName() != null && repository.existsByNameIgnoreCase(dto.getName())) {
            throw new ValidationException("Contact name already exists: %s", dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && repository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ValidationException("Contact email already exists: %s", dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank() && repository.existsByPhone(dto.getPhone())) {
            throw new ValidationException("Contact phone already exists: %s", dto.getPhone());
        }
        ContactsEntity entity = mapper.toEntity(dto);
        if (dto.getCompanyId() != null) {
            CompanyEntity company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new ValidationException("Company not found with id: %s", dto.getCompanyId()));
            entity.setCompany(company);
        }
        ContactsEntity saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public Contacts update(Long id, Contacts dto) {
        if (dto.getName() != null && repository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new ValidationException("Contact name already exists: %s", dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && repository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail(), id)) {
            throw new ValidationException("Contact email already exists: %s", dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank() && repository.existsByPhoneAndIdNot(dto.getPhone(), id)) {
            throw new ValidationException("Contact phone already exists: %s", dto.getPhone());
        }
        ContactsEntity entity = repository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Entity not found with id " + id));
        mapper.updateEntity(dto, entity);
        
        // Handle company relationship
        if (dto.getCompanyId() != null) {
            CompanyEntity company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new ValidationException("Company not found with id: %s", dto.getCompanyId()));
            entity.setCompany(company);
        } else {
            entity.setCompany(null);
        }
        
        ContactsEntity saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    public Contacts getContactByName(String name) {
        assert repository != null;
        ContactsEntity entity = repository.findByName(name)
                .orElseThrow(() -> new ContactExceptions.ContactNotFoundException(name));

        return mapper.toDto(entity);
    }

    public Contacts getContactById(Long id) {
        assert repository != null;
        ContactsEntity entity = repository.findById(id)
                .orElseThrow(() -> new ContactExceptions.ContactNotFoundException(id));

        return mapper.toDto(entity);
    }

    public ContactValidationResponse validateAvailability(String name, String email, String phone, Long excludeId) {
        boolean nameOk = true;
        boolean emailOk = true;
        boolean phoneOk = true;
        String nameReason = "OK";
        String emailReason = "OK";
        String phoneReason = "OK";

        if (name != null && !name.isBlank()) {
            nameOk = excludeId == null
                    ? !repository.existsByNameIgnoreCase(name)
                    : !repository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
            if (!nameOk) nameReason = "Name already exists";
        }
        if (email != null && !email.isBlank()) {
            emailOk = excludeId == null
                    ? !repository.existsByEmailIgnoreCase(email)
                    : !repository.existsByEmailIgnoreCaseAndIdNot(email, excludeId);
            if (!emailOk) emailReason = "Email already exists";
        }
        if (phone != null && !phone.isBlank()) {
            phoneOk = excludeId == null
                    ? !repository.existsByPhone(phone)
                    : !repository.existsByPhoneAndIdNot(phone, excludeId);
            if (!phoneOk) phoneReason = "Phone already exists";
        }

        return ContactValidationResponse.builder()
                .nameAvailable(nameOk)
                .emailAvailable(emailOk)
                .phoneAvailable(phoneOk)
                .nameReason(nameReason)
                .emailReason(emailReason)
                .phoneReason(phoneReason)
                .build();
    }

    public List<Contacts> findCustomers() {
        List<ContactsEntity> entities = repository.findByCustomerTrue();
        return mapper.toDtoList(entities);
    }

    public List<Contacts> findClients() {
        List<ContactsEntity> entities = repository.findByClientTrue();
        return mapper.toDtoList(entities);
    }
}
