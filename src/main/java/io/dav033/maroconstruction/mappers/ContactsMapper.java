package io.dav033.maroconstruction.mappers;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.models.ContactsEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContactsMapper extends GenericMapper<Contacts, ContactsEntity> {
}
