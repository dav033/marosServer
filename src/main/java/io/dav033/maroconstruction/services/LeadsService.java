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
import io.dav033.maroconstruction.models.ProjectEntity;
import io.dav033.maroconstruction.models.ProjectTypeEntity;
import io.dav033.maroconstruction.repositories.ContactsRepository;
import io.dav033.maroconstruction.repositories.LeadsRepository;
import io.dav033.maroconstruction.repositories.ProjectRepository;
import io.dav033.maroconstruction.repositories.ProjectTypeRepository;
import io.dav033.maroconstruction.services.base.BaseService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import io.dav033.maroconstruction.dto.responses.LeadNumberValidationResponse;
import org.springframework.util.StringUtils;

@Service
public class LeadsService extends BaseService<Leads, Long, LeadsEntity, LeadsRepository> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LeadsService.class);

    public LeadsService(
            LeadsRepository repository,
            GenericMapper<Leads, LeadsEntity> mapper,
            ContactsService contactsService,
            ContactsRepository contactsRepository,
            ProjectTypeRepository projectTypeRepository,
            ProjectRepository projectRepository,
            LeadsMapper leadMapper,
            LeadClickUpSyncService leadClickUpSyncService,
            EntityManager entityManager) {
        super(repository, mapper);
        this.contactsService = contactsService;
        this.contactsRepository = contactsRepository;
        this.projectTypeRepository = projectTypeRepository;
        this.projectRepository = projectRepository;
        this.leadMapper = leadMapper;
        this.leadClickUpSyncService = leadClickUpSyncService;
        this.entityManager = entityManager;
    }

    private static final DateTimeFormatter LEAD_NO_FMT = DateTimeFormatter.ofPattern("MMyy");

    private final ContactsService contactsService;
    private final ContactsRepository contactsRepository;
    private final ProjectTypeRepository projectTypeRepository;
    private final ProjectRepository projectRepository;
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

    @Transactional(readOnly = true)
    public Leads getLeadById(Long id) {
        LeadsEntity entity = repository.findById(id)
                .orElseThrow(() -> new LeadExceptions.LeadNotFoundException(id));
        // Forzar carga de relaciones lazy
        if (entity.getContact() != null) {
            entity.getContact().getName();
        }
        if (entity.getProjectType() != null) {
            entity.getProjectType().getName();
        }
        return leadMapper.toDto(entity);
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
        
        // Auto-generate lead name if empty: {leadNumber}-{location}
        if (lead.getName() == null || lead.getName().trim().isEmpty()) {
            if (lead.getLocation() != null && !lead.getLocation().trim().isEmpty()) {
                lead.setName(lead.getLeadNumber() + "-" + lead.getLocation().trim());
            } else {
                throw new io.dav033.maroconstruction.exceptions.ValidationException(
                    "Location is required when lead name is not provided");
            }
        }
        
        // Validate location is not empty
        if (lead.getLocation() == null || lead.getLocation().trim().isEmpty()) {
            throw new io.dav033.maroconstruction.exceptions.ValidationException(
                "Location is required");
        }
        
        // Get projectTypeId from ProjectType object or throw error if not present
        Long projectTypeId = null;
        if (lead.getProjectType() != null && lead.getProjectType().getId() != null) {
            projectTypeId = lead.getProjectType().getId();
        }
        
        if (projectTypeId == null) {
            throw new io.dav033.maroconstruction.exceptions.ValidationException(
                "Project Type is required");
        }
        
        ProjectTypeEntity projectType = resolveProjectType(projectTypeId);

        LeadsEntity entity = leadMapper.toEntity(lead);
        entity.setContact(contact);
        entity.setProjectType(projectType);

        try {
            LeadsEntity saved = repository.save(entity);
            Leads dto = leadMapper.toDto(saved);
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
        if (patch.getLeadNumber() != null && !patch.getLeadNumber().equals(entity.getLeadNumber())) {
            if (repository.existsByLeadNumberAndIdNot(patch.getLeadNumber(), id)) {
                throw new io.dav033.maroconstruction.exceptions.ValidationException(
                        "Lead number already exists: %s", patch.getLeadNumber());
            }
        }

        System.out.println("[LOG] Notas recibidas en servicio: " + patch.getNotes());
        updateEntityFields(patch, entity);
        System.out.println("[LOG] Notas persistidas en entidad: " + entity.getNotes());
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
        // Manejo robusto de startDate:
        if (dto.getStartDate() != null) {
            // Si el cliente envía una fecha, se respeta
            entity.setStartDate(dto.getStartDate());
        } else if (entity.getStartDate() == null) {
            // Si la entidad viene con startDate = null (datos antiguos)
            // y el cliente no envía fecha, asignamos un valor por defecto
            entity.setStartDate(LocalDate.now());
        }
        if (dto.getLocation() != null) {
            entity.setLocation(dto.getLocation());
        }
        if (dto.getStatus() != null) {
            boolean valid = false;
            for (LeadStatus s : LeadStatus.values()) {
                if (s.name().equals(dto.getStatus().name())) {
                    valid = true;
                    break;
                }
            }
            if (valid) {
                entity.setStatus(dto.getStatus());
            } else {
                entity.setStatus(LeadStatus.NOT_EXECUTED);
            }
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
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }
    }

    @Transactional
    public boolean deleteLead(Long id) {
        LeadsEntity entity = repository.findById(id)
                .orElseThrow(() -> new LeadExceptions.LeadNotFoundException(id));
        Leads dto = leadMapper.toDto(entity);
        var syncResult = leadClickUpSyncService.syncLeadDelete(dto);
        if (!isDeleteSyncSuccessful(syncResult)) {
            String errorMsg = String.format("Failed to sync lead deletion with ClickUp. Status: %s, Diagnosis: %s",
                    syncResult.getStatus(), syncResult.getDiagnosis());
            log.error(errorMsg);
            throw new RuntimeException("ClickUp sync failed: " + errorMsg);
        }

        log.info("ClickUp sync successful for lead {}. Status: {}, TaskId: {}",
                id, syncResult.getStatus(), syncResult.getTaskId());

        try {
            // Clear lead references from projects before deletion
            List<ProjectEntity> projects = projectRepository.findByLeadId(id);
            for (ProjectEntity project : projects) {
                project.setLead(null);
                projectRepository.save(project);
            }
            
            // Cargar la entidad completamente con todas las relaciones usando FETCH
            LeadsEntity lead = repository.findByIdWithRelations(id)
                    .orElseThrow(() -> new LeadExceptions.LeadNotFoundException(id));
            
            repository.delete(lead);
            return true;
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseException("Cannot delete lead due to existing references", ex);
        }
    }

    private boolean isDeleteSyncSuccessful(Object syncResult) {
        try {
            var statusField = syncResult.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            String status = (String) statusField.get(syncResult);
            return "DELETED".equals(status) ||
                    "NOT_FOUND".equals(status) ||
                    "CONFIG_ERROR".equals(status);
        } catch (Exception e) {
            log.warn("Could not check sync result status, assuming success", e);
            return true;
        }
    }

    private void applyDefaults(Leads lead) {
        lead.setId(null);
        lead.setStatus(Optional.ofNullable(lead.getStatus()).orElse(LeadStatus.NOT_EXECUTED));
        // Inicializar startDate si no viene informado
        if (lead.getStartDate() == null) {
            lead.setStartDate(LocalDate.now());
        }
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
                    if (type == LeadType.PLUMBING && s.matches("^\\d{3}P-\\d{4}$"))
                        return Integer.parseInt(s.substring(0, 3));
                    if (type == LeadType.CONSTRUCTION && s.matches("^\\d{3}-\\d{4}$"))
                        return Integer.parseInt(s.substring(0, 3));
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

    public LeadNumberValidationResponse validateLeadNumber(String leadNumber) {
        if (!StringUtils.hasText(leadNumber)) {
            return LeadNumberValidationResponse.builder()
                    .valid(false)
                    .reason("Lead number is required")
                    .build();
        }

        String trimmedLeadNumber = leadNumber.trim();
        boolean exactExists = repository.existsByLeadNumber(trimmedLeadNumber);
        if (exactExists) {
            return LeadNumberValidationResponse.builder()
                    .valid(false)
                    .reason("Lead number already exists")
                    .build();
        }
        String numericPrefix = extractNumericPrefix(trimmedLeadNumber);
        if (numericPrefix == null) {
            return LeadNumberValidationResponse.builder()
                    .valid(false)
                    .reason("Invalid lead number format")
                    .build();
        }
        boolean prefixInUse = isNumericPrefixInUse(numericPrefix);
        if (prefixInUse) {
            return LeadNumberValidationResponse.builder()
                    .valid(false)
                    .reason("Lead number prefix " + numericPrefix + " is already in use")
                    .build();
        }

        return LeadNumberValidationResponse.builder()
                .valid(true)
                .reason("OK")
                .build();
    }

    private String extractNumericPrefix(String leadNumber) {
        if (leadNumber.matches("^\\d{3}P-\\d{4}$") || leadNumber.matches("^\\d{3}-\\d{4}$")) {
            return leadNumber.substring(0, 3);
        }
        return null;
    }

    private boolean isNumericPrefixInUse(String numericPrefix) {
        for (LeadType type : LeadType.values()) {
            List<String> allNumbers = repository.findAllLeadNumbersByType(type);
            boolean prefixExists = allNumbers.stream()
                    .filter(s -> s != null)
                    .anyMatch(s -> {
                        String existingPrefix = extractNumericPrefix(s);
                        return numericPrefix.equals(existingPrefix);
                    });

            if (prefixExists) {
                return true;
            }
        }
        return false;
    }
}