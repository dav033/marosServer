package io.dav033.maroconstruction.mappers;

import io.dav033.maroconstruction.dto.Leads;
import io.dav033.maroconstruction.models.LeadsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface LeadsMapper extends GenericMapper<Leads, LeadsEntity> {

	@Override
	@Mapping(target = "notes", source = "notes")
	Leads toDto(LeadsEntity entity);

	@Override
	@Mapping(target = "notes", source = "notes")
	LeadsEntity toEntity(Leads dto);
}
