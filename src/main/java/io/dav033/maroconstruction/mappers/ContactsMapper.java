package io.dav033.maroconstruction.mappers;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.models.ContactsEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.ArrayList;

@Mapper(componentModel = "spring")
public interface ContactsMapper extends GenericMapper<Contacts, ContactsEntity> {

    @Override
    @Mapping(source = "customer", target = "customer")
    @Mapping(source = "client", target = "client")
    @Mapping(source = "companyId", target = "company", ignore = true)
    @Mapping(source = "notes", target = "notes")
    ContactsEntity toEntity(Contacts dto);

    @Override
    @Mapping(source = "customer", target = "customer")
    @Mapping(source = "client", target = "client")
    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "notes", target = "notes")
    Contacts toDto(ContactsEntity entity);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(
        source = "phone",
        target = "phone",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
    )
    @Mapping(
        source = "email",
        target = "email",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
    )
    @Mapping(
        source = "occupation",
        target = "occupation",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
    )
    @Mapping(
        source = "address",
        target = "address",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
    )
    @Mapping(source = "customer", target = "customer")
    @Mapping(source = "client", target = "client")
    // 👇 Muy importante: ignoramos notes en el mapeo automático del update
    @Mapping(target = "notes", ignore = true)
    void updateEntity(Contacts dto, @MappingTarget ContactsEntity entity);

    @AfterMapping
    default void updateNotes(Contacts dto, @MappingTarget ContactsEntity entity) {
        if (dto.getNotes() != null) {
            // Forzamos a que se reemplace la lista de notas completa
            entity.setNotes(new ArrayList<>(dto.getNotes()));
        }
    }
}
