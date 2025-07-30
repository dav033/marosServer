package io.dav033.maroconstruction.mappers;

import io.dav033.maroconstruction.dto.ProjectType;
import io.dav033.maroconstruction.models.ProjectTypeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectTypeMapper extends GenericMapper<ProjectType, ProjectTypeEntity> {

}
