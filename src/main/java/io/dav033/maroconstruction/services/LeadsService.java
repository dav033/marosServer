package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.dto.Leads;
import io.dav033.maroconstruction.enums.LeadStatus;
import io.dav033.maroconstruction.enums.LeadType;
import io.dav033.maroconstruction.exceptions.ContactExceptions;
import io.dav033.maroconstruction.exceptions.DatabaseException;
import io.dav033.maroconstruction.exceptions.LeadExceptions;
import io.dav033.maroconstruction.exceptions.ProjectTypeExceptions;
import io.dav033.maroconstruction.mappers.GenericMapper;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import io.dav033.maroconstruction.dto.responses.LeadNumberValidationResponse;
import org.springframework.util.StringUtils;

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
            LeadClickUpSyncService leadClickUpSyncService,
            EntityManager entityManager) {
        super(repository, mapper);
        this.contactsService = contactsService;
        this.contactsRepository = contactsRepository;
        this.projectTypeRepository = projectTypeRepository;
        this.leadMapper = leadMapper;
        this.leadClickUpSyncService = leadClickUpSyncService;
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
    private final LeadClickUpSyncService leadClickUpSyncService;

    @PersistenceContext
    private final EntityManager entityManager;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * CONSULTAS
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    public List<Leads> getAllLeads() {
        return repository.findAll()
                .stream()
                .map(leadMapper::toDto)
                .toList();
    }

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
        return createLeadWithNewContact(lead, contact, false);
    }

    @Transactional
    public Leads createLeadWithNewContact(Leads lead, Contacts contact, boolean skipClickUpSync) {
        Contacts saved = contactsService.create(contact);
        ContactsEntity contactRef = contactsRepository.getReferenceById(saved.getId());
        return persistLead(lead, contactRef, skipClickUpSync);
    }

    @Transactional
    public Leads createLeadWithExistingContact(Leads lead, Long contactId) {
        return createLeadWithExistingContact(lead, contactId, false);
    }

    @Transactional
    public Leads createLeadWithExistingContact(Leads lead, Long contactId, boolean skipClickUpSync) {
        ContactsEntity contact = contactsRepository.findById(contactId)
                .orElseThrow(() -> new ContactExceptions.ContactNotFoundException(contactId));
        return persistLead(lead, contact, skipClickUpSync);
    }

    private Leads persistLead(Leads lead, ContactsEntity contact, boolean skipClickUpSync) {
        applyDefaults(lead);
        ProjectTypeEntity projectType = resolveProjectType(lead.getProjectType().getId());

        LeadsEntity entity = leadMapper.toEntity(lead);
        entity.setContact(contact);
        entity.setProjectType(projectType);

        try {
            LeadsEntity saved = repository.save(entity);
            Leads dto = leadMapper.toDto(saved);
            
            // Sincronizar con ClickUp después de crear (si no se omite)
            if (!skipClickUpSync) {
                leadClickUpSyncService.syncLeadCreate(dto);
            } else {
                log.info("Skip ClickUp sync on create for lead {} ({})", dto.getId(), dto.getLeadNumber());
            }
            
            return dto;
        } catch (DataIntegrityViolationException ex) {
            throw new LeadExceptions.LeadCreationException("Data integrity error creating lead", ex);
        }
    }

    @Transactional
    public Leads updateLead(Long id, Leads patch) {
        LeadsEntity entity = repository.findById(id)
                .orElseThrow(() -> new LeadExceptions.LeadNotFoundException(id));

        // Validar leadNumber duplicado si viene en el patch y cambia
        if (patch.getLeadNumber() != null && !patch.getLeadNumber().equals(entity.getLeadNumber())) {
            if (repository.existsByLeadNumberAndIdNot(patch.getLeadNumber(), id)) {
                throw new io.dav033.maroconstruction.exceptions.ValidationException(
                        "Lead number already exists: %s", patch.getLeadNumber());
            }
        }

        updateEntityFields(patch, entity);
        entityManager.flush();

        Leads dto = leadMapper.toDto(entity);
        leadClickUpSyncService.syncLeadUpdate(dto);
        return dto;
    }

    private void updateEntityFields(Leads dto, LeadsEntity entity) {
        if (dto.getLeadNumber() != null) {
            entity.setLeadNumber(dto.getLeadNumber());
        }
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getStartDate() != null) {
            entity.setStartDate(dto.getStartDate());
        }
        if (dto.getLocation() != null) {
            entity.setLocation(dto.getLocation());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getLeadType() != null) {
            entity.setLeadType(dto.getLeadType());
        }
        if (dto.getContact() != null && dto.getContact().getId() != null) {
            ContactsEntity contactEntity = contactsRepository.findById(dto.getContact().getId())
                    .orElseThrow(() -> new ContactExceptions.ContactNotFoundException(dto.getContact().getId()));
            entity.setContact(contactEntity);
        }
        if (dto.getProjectType() != null && dto.getProjectType().getId() != null) {
            ProjectTypeEntity projectTypeEntity = projectTypeRepository.findById(dto.getProjectType().getId())
                    .orElseThrow(
                            () -> new ProjectTypeExceptions.ProjectTypeNotFoundException(dto.getProjectType().getId()));
            entity.setProjectType(projectTypeEntity);
        }
    }

    @Transactional
    public boolean deleteLead(Long id) {
        LeadsEntity entity = repository.findById(id)
                .orElseThrow(() -> new LeadExceptions.LeadNotFoundException(id));
        
        // Convertir a DTO y sincronizar eliminación con ClickUp antes de eliminar
        Leads dto = leadMapper.toDto(entity);
        leadClickUpSyncService.syncLeadDelete(dto);
        
        try {
            repository.deleteById(id);
            return true;
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseException("Cannot delete lead due to existing references", ex);
        }
    }

    private void applyDefaults(Leads lead) {
        lead.setId(null);
        lead.setStatus(Optional.ofNullable(lead.getStatus()).orElse(LeadStatus.TO_DO));
        // Si el cliente no envía leadNumber, autogenerar. Si envía, validar duplicado.
        if (!StringUtils.hasText(lead.getLeadNumber())) {
            lead.setLeadNumber(generateLeadNumber(lead.getLeadType()));
        } else if (repository.existsByLeadNumber(lead.getLeadNumber())) {
            throw new io.dav033.maroconstruction.exceptions.ValidationException(
                    "Lead number already exists: %s", lead.getLeadNumber());
        }
    }

    private String generateLeadNumber(LeadType type) {
        String mmyy = LocalDate.now().format(LEAD_NO_FMT);
        List<String> all = repository.findAllLeadNumbersByType(type);

        int max = all.stream()
            .filter(s -> s != null)
            .map(s -> {
                if (type == LeadType.PLUMBING && s.matches("^\\d{3}P-\\d{4}$")) return Integer.parseInt(s.substring(0,3));
                if (type == LeadType.CONSTRUCTION && s.matches("^\\d{3}-\\d{4}$")) return Integer.parseInt(s.substring(0,3));
                return -1;
            })
            .filter(i -> i >= 0)
            .max(Integer::compareTo)
            .orElse(0);

        int next = max + 1;
        String base = String.format("%03d", next);
        return (type == LeadType.PLUMBING) ? base + "P-" + mmyy : base + "-" + mmyy;
    }

    private ProjectTypeEntity resolveProjectType(Long id) {
        return projectTypeRepository.findById(id)
                .orElseThrow(() -> new ProjectTypeExceptions.ProjectTypeNotFoundException(id));
    }

    // === Validación de leadNumber ===
    public LeadNumberValidationResponse validateLeadNumber(String leadNumber) {
        if (!StringUtils.hasText(leadNumber)) {
            return LeadNumberValidationResponse.builder()
                    .valid(false)
                    .reason("Lead number is required")
                    .build();
        }
        boolean exists = repository.existsByLeadNumber(leadNumber.trim());
        return LeadNumberValidationResponse.builder()
                .valid(!exists)
                .reason(exists ? "Lead number already exists" : "OK")
                .build();
    }
}