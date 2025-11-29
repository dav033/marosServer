package io.dav033.maroconstruction.mappers;

import io.dav033.maroconstruction.dto.Company;
import io.dav033.maroconstruction.models.CompanyEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CompanyMapper extends GenericMapper<Company, CompanyEntity> {

    // =========================
    // CREAR (DTO -> Entity)
    // =========================
    @Override
    @Mapping(source = "customer", target = "customer")
    @Mapping(source = "client", target = "client")
    @Mapping(source = "notes", target = "notes")
    CompanyEntity toEntity(Company dto);

    // =========================
    // LEER (Entity -> DTO)
    // =========================
    @Override
    @Mapping(source = "customer", target = "customer")
    @Mapping(source = "client", target = "client")
    @Mapping(source = "notes", target = "notes")
    Company toDto(CompanyEntity entity);

    // =========================
    // ACTUALIZAR (DTO -> Entity existente)
    // =========================
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "customer", target = "customer")
    @Mapping(source = "client", target = "client")
    // Muy importante: MapStruct NO debe intentar hacer addAll sobre la lista de notas
    @Mapping(target = "notes", ignore = true)
    void updateEntity(Company dto, @MappingTarget CompanyEntity entity);

    // =========================
    // MANEJO MANUAL DE NOTES
    // =========================
    @AfterMapping
    default void updateNotes(Company dto, @MappingTarget CompanyEntity entity) {
        if (dto.getNotes() != null) {
            // Usamos directamente el setter de la entidad, que se encarga de guardar en notesJson
            entity.setNotes(dto.getNotes());
        }
        // Si dto.getNotes() es null, no tocamos las notas en la entidad (las dejamos como están)
    }
}
