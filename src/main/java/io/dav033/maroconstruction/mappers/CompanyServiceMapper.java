package io.dav033.maroconstruction.mappers;

import io.dav033.maroconstruction.dto.CompanyService;
import io.dav033.maroconstruction.models.CompanyServiceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyServiceMapper extends GenericMapper<CompanyService, CompanyServiceEntity> {
}
