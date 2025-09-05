package io.dav033.maroconstruction.mappers;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.models.ContactsEntity;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ContactsMapper extends GenericMapper<Contacts, ContactsEntity> {

	@Override
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	void updateEntity(Contacts dto, @MappingTarget ContactsEntity entity);
}
