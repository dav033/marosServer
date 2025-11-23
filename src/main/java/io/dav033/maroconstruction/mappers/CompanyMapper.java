package io.dav033.maroconstruction.mappers;

import io.dav033.maroconstruction.dto.Company;
import io.dav033.maroconstruction.models.CompanyEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CompanyMapper extends GenericMapper<Company, CompanyEntity> {

    @Override
    @Mapping(target = "service", ignore = true)
    CompanyEntity toEntity(Company dto);

    @Override
    @Mapping(source = "service.id", target = "serviceId")
    Company toDto(CompanyEntity entity);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "service", ignore = true)
    void updateEntity(Company dto, @MappingTarget CompanyEntity entity);
}
