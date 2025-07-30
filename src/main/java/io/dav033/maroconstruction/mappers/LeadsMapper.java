package io.dav033.maroconstruction.mappers;

import io.dav033.maroconstruction.dto.Leads;
import io.dav033.maroconstruction.models.LeadsEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LeadsMapper extends GenericMapper<Leads, LeadsEntity> {
	
}
