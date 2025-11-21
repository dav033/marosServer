package io.dav033.maroconstruction.mappers;

import io.dav033.maroconstruction.dto.Project;
import io.dav033.maroconstruction.models.ProjectEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper extends GenericMapper<Project, ProjectEntity> {
}
