package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.ProjectType;
import io.dav033.maroconstruction.mappers.ProjectTypeMapper;
import io.dav033.maroconstruction.models.ProjectTypeEntity;
import io.dav033.maroconstruction.repositories.ProjectTypeRepository;
import io.dav033.maroconstruction.services.base.BaseService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProjectTypeService
        extends BaseService<
        ProjectType,
        Long,
        ProjectTypeEntity,
        ProjectTypeRepository
        > {

    public ProjectTypeService(
            ProjectTypeRepository repository,
            @Qualifier("projectTypeMapperImpl") ProjectTypeMapper mapper
    ) {
        super(repository, mapper);
    }
}
