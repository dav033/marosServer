package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.exceptions.ResourceNotFoundException;
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

    public Contacts getContactByName(String name) {
        assert repository != null;
        ContactsEntity entity = repository.findByName(name)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                String.format("Contacto con nombre '%s' no encontrado", name))
                );

        return mapper.toDto(entity);
    }


    public Contacts getContactById(Long id) {
        assert repository != null;
        ContactsEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                String.format("Contacto con ID '%d' no encontrado", id))
                );

        return mapper.toDto(entity);

    }

}
