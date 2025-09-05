package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.exceptions.ContactExceptions;
import io.dav033.maroconstruction.exceptions.ValidationException;
import io.dav033.maroconstruction.dto.responses.ContactValidationResponse;
import io.dav033.maroconstruction.mappers.ContactsMapper;
import io.dav033.maroconstruction.models.ContactsEntity;
import io.dav033.maroconstruction.repositories.ContactsRepository;
import io.dav033.maroconstruction.services.base.BaseService;
import org.springframework.stereotype.Service;

@Service
public class ContactsService extends BaseService<Contacts, Long, ContactsEntity, ContactsRepository> {

    public ContactsService(ContactsRepository repository, ContactsMapper contactsMapper) {
        super(repository, contactsMapper);
    }

    @Override
    public Contacts create(Contacts dto) {
        // Uniqueness validation: name (ci), email (ci), phone
        if (dto.getName() != null && repository.existsByNameIgnoreCase(dto.getName())) {
            throw new ValidationException("Contact name already exists: %s", dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && repository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ValidationException("Contact email already exists: %s", dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank() && repository.existsByPhone(dto.getPhone())) {
            throw new ValidationException("Contact phone already exists: %s", dto.getPhone());
        }
        return super.create(dto);
    }

    @Override
    public Contacts update(Long id, Contacts dto) {
        // For update, exclude current id
        if (dto.getName() != null && repository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new ValidationException("Contact name already exists: %s", dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && repository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail(), id)) {
            throw new ValidationException("Contact email already exists: %s", dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank() && repository.existsByPhoneAndIdNot(dto.getPhone(), id)) {
            throw new ValidationException("Contact phone already exists: %s", dto.getPhone());
        }
        return super.update(id, dto);
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
}
