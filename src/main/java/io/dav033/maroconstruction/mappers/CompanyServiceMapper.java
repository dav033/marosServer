package io.dav033.maroconstruction.mappers;

import io.dav033.maroconstruction.dto.CompanyService;
import io.dav033.maroconstruction.models.CompanyServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyServiceMapper extends GenericMapper<CompanyService, CompanyServiceEntity> {
    
    @Override
    @Mapping(target = "id", ignore = true)
    void updateEntity(CompanyService dto, @MappingTarget CompanyServiceEntity entity);
}
