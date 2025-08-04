package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.dto.LeadPayloadDto;
import io.dav033.maroconstruction.dto.Leads;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.enums.LeadStatus;
import io.dav033.maroconstruction.enums.LeadType;
import io.dav033.maroconstruction.exceptions.ContactExceptions;
import io.dav033.maroconstruction.exceptions.DatabaseException;
import io.dav033.maroconstruction.exceptions.LeadExceptions;
import io.dav033.maroconstruction.exceptions.ProjectTypeExceptions;
import io.dav033.maroconstruction.mappers.ContactsMapper;
import io.dav033.maroconstruction.mappers.GenericMapper;
import io.dav033.maroconstruction.mappers.LeadToClickUpTaskMapper;
import io.dav033.maroconstruction.mappers.LeadsMapper;
import io.dav033.maroconstruction.models.ContactsEntity;
import io.dav033.maroconstruction.models.LeadsEntity;
import io.dav033.maroconstruction.models.ProjectTypeEntity;
import io.dav033.maroconstruction.repositories.ContactsRepository;
import io.dav033.maroconstruction.repositories.LeadsRepository;
import io.dav033.maroconstruction.repositories.ProjectTypeRepository;
import io.dav033.maroconstruction.services.base.BaseService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeadsService extends BaseService<Leads, Long, LeadsEntity, LeadsRepository> {

    public LeadsService(
            LeadsRepository repository,
            GenericMapper<Leads, LeadsEntity> mapper,
            ContactsService contactsService,
            ContactsRepository contactsRepository,
            ProjectTypeRepository projectTypeRepository,
            LeadsMapper leadMapper,
            ClickUpService clickUpService,
            LeadToClickUpTaskMapper taskMapper,
            EntityManager entityManager
    ) {
        super(repository, mapper);
        this.contactsService = contactsService;
        this.contactsRepository = contactsRepository;
        this.projectTypeRepository = projectTypeRepository;
        this.leadMapper = leadMapper;
        this.clickUpService = clickUpService;
        this.taskMapper = taskMapper;
        this.entityManager = entityManager;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * CONSTANTES
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    private static final DateTimeFormatter LEAD_NO_FMT = DateTimeFormatter.ofPattern("MMyy");

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * DEPENDENCIAS
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    private final ContactsService contactsService;
    private final ContactsRepository contactsRepository;
    private final ProjectTypeRepository projectTypeRepository;
    private final LeadsMapper leadMapper;
    private final ClickUpService clickUpService;
    private final LeadToClickUpTaskMapper taskMapper;

    @PersistenceContext
    private final EntityManager entityManager;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * CONSULTAS
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    public List<Leads> getLeadsByType(LeadType type) {
        return repository.findByLeadType(type)
                .stream()
                .map(leadMapper::toDto)
                .toList();
    }

    public List<String> getAllLeadNumbers(LeadType type) {
        return repository.findAllLeadNumbersByType(type);
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * CREACIÓN
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    @Transactional
    public Leads createLeadWithNewContact(Leads lead, Contacts contact) {
        Contacts saved = contactsService.create(contact);
        ContactsEntity contactRef = contactsRepository.getReferenceById(saved.getId());
        return persistLead(lead, contactRef);
    }

    @Transactional
    public Leads createLeadWithExistingContact(Leads lead, Long contactId) {
        ContactsEntity contact = contactsRepository.findById(contactId)
                .orElseThrow(() -> new ContactExceptions.ContactNotFoundException(contactId));
        return persistLead(lead, contact);
    }

    private Leads persistLead(Leads lead, ContactsEntity contact) {
        applyDefaults(lead);
        ProjectTypeEntity projectType = resolveProjectType(lead.getProjectType().getId());

        LeadsEntity entity = leadMapper.toEntity(lead);
        entity.setContact(contact);
        entity.setProjectType(projectType);

        try {
            LeadsEntity saved = repository.save(entity);
            return leadMapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new LeadExceptions.LeadCreationException("Data integrity error creating lead", ex);
        }
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * ACTUALIZACIÓN
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    @Transactional
    public Leads updateLead(Long id, Leads patch) {
        LeadsEntity entity = repository.findById(id)
                .orElseThrow(() -> new LeadExceptions.LeadNotFoundException(id));

        leadMapper.updateEntity(patch, entity);
        entityManager.flush();

        Leads dto = leadMapper.toDto(entity);
        syncWithClickUp(dto);
        return dto;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * ELIMINACIÓN
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    @Transactional
    public boolean deleteLead(Long id) {
        if (!repository.existsById(id)) {
            throw new LeadExceptions.LeadNotFoundException(id);
        }
        try {
            repository.deleteById(id);
            return true;
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseException("Cannot delete lead due to existing references", ex);
        }
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * UTILIDADES PRIVADAS
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    private void applyDefaults(Leads lead) {
        lead.setId(null);
        lead.setStatus(Optional.ofNullable(lead.getStatus()).orElse(LeadStatus.TO_DO));
        lead.setLeadNumber(generateLeadNumber(lead.getLeadType()));
    }

    private String generateLeadNumber(LeadType type) {
        String monthYear = LocalDate.now().format(LEAD_NO_FMT);
        int nextSeq = repository.findMaxSequenceForMonth(type, monthYear).orElse(0) + 1;
        return "%03d-%s".formatted(nextSeq, monthYear);
    }

    private ProjectTypeEntity resolveProjectType(Long id) {
        return projectTypeRepository.findById(id)
                .orElseThrow(() -> new ProjectTypeExceptions.ProjectTypeNotFoundException(id));
    }

    private void syncWithClickUp(Leads lead) {
        if (!clickUpService.isConfigured())
            return;
        try {
            String taskId = clickUpService.findTaskIdByLeadNumber(lead.getLeadNumber());
            if (taskId == null) {
                log.warn("No ClickUp task found for leadNumber={}", lead.getLeadNumber());
                return;
            }
            LeadPayloadDto payload = LeadPayloadDto.builder()
                    .leadNumber(lead.getLeadNumber())
                    .name(lead.getName())
                    .location(lead.getLocation())
                    .startDate(Optional.ofNullable(lead.getStartDate()).map(Object::toString).orElse(null))
                    .leadType(Optional.ofNullable(lead.getLeadType()).map(Enum::name).orElse(null))
                    .contactId(Optional.ofNullable(lead.getContact()).map(Contacts::getId).orElse(null))
                    .build();

            ClickUpTaskRequest req = taskMapper.toClickUpTask(payload);
            clickUpService.updateTask(taskId, req);
            log.info("Lead synced to ClickUp → taskId={} leadNumber={}", taskId, lead.getLeadNumber());
        } catch (Exception ex) {
            log.error("Failed to sync lead {} with ClickUp: {}", lead.getId(), ex.getMessage(), ex);
        }
    }
}