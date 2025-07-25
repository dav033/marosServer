package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.Leads;
import io.dav033.maroconstruction.enums.LeadType;
import io.dav033.maroconstruction.mappers.LeadsMapper;
import io.dav033.maroconstruction.models.LeadsEntity;
import io.dav033.maroconstruction.repositories.LeadsRepository;
import io.dav033.maroconstruction.services.base.BaseService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LeadsService
        extends BaseService<Leads, Long, LeadsEntity, LeadsRepository> {

    private final LeadsMapper leadMapper;

    public LeadsService(LeadsRepository repository,
                        @Qualifier("leadsMapperImpl") LeadsMapper leadMapper) {
        super(repository, leadMapper);
        this.leadMapper = leadMapper;
    }

    /**
     * Recupera todos los Leads de un tipo dado,
     * incluyendo Contact y ProjectType poblados.
     */
    public List<Leads> getLeadsByType(LeadType leadType) {
        List<LeadsEntity> entities = repository.findByLeadType(leadType);
        return entities.stream()
                .map(leadMapper::toDto)
                .collect(Collectors.toList());
    }
}
