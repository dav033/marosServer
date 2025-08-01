package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.dto.Leads;
import io.dav033.maroconstruction.enums.LeadStatus;
import io.dav033.maroconstruction.enums.LeadType;
import io.dav033.maroconstruction.exceptions.ContactExceptions;
import io.dav033.maroconstruction.exceptions.DatabaseException;
import io.dav033.maroconstruction.exceptions.LeadExceptions;
import io.dav033.maroconstruction.exceptions.ProjectTypeExceptions;
import io.dav033.maroconstruction.mappers.LeadsMapper;
import io.dav033.maroconstruction.models.ContactsEntity;
import io.dav033.maroconstruction.models.LeadsEntity;
import io.dav033.maroconstruction.models.ProjectTypeEntity;
import io.dav033.maroconstruction.repositories.ContactsRepository;
import io.dav033.maroconstruction.repositories.LeadsRepository;
import io.dav033.maroconstruction.repositories.ProjectTypeRepository;
import io.dav033.maroconstruction.services.base.BaseService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class LeadsService
        extends BaseService<Leads, Long, LeadsEntity, LeadsRepository> {

    private final ContactsService contactsService;
    private final ContactsRepository contactsRepository;
    private final ProjectTypeRepository projectTypeRepository;
    private final LeadsMapper leadMapper;

    public LeadsService(LeadsRepository repository, ContactsService contactsService,
            ContactsRepository contactsRepository,
            ProjectTypeRepository projectTypeRepository,
            @Qualifier("leadsMapperImpl") LeadsMapper leadMapper) {
        super(repository, leadMapper);
        this.contactsService = contactsService;
        this.contactsRepository = contactsRepository;
        this.projectTypeRepository = projectTypeRepository;
        this.leadMapper = leadMapper;
    }

    public List<Leads> getLeadsByType(LeadType leadType) {
        List<LeadsEntity> entities = repository.findByLeadType(leadType);
        return entities.stream()
                .map(leadMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<String> getAllLeadNumbers(LeadType leadType) {
        return repository.findAllLeadNumbersByType(leadType);
    }

    private String generateLeadNumber(LeadType leadType) {
        LocalDate now = LocalDate.now();
        String monthYear = now.format(DateTimeFormatter.ofPattern("MMyy"));
        
        List<String> allLeadNumbers = getAllLeadNumbers(leadType);
        
        int nextSequence = 1;
        if (!allLeadNumbers.isEmpty()) {
      
            List<Integer> sequences = allLeadNumbers.stream()
                .map(leadNumber -> {
                    try {
                        String[] parts = leadNumber.split("-");
                        if (parts.length == 2) {
                            return Integer.parseInt(parts[0]);
                        }
                        return 0;
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .filter(seq -> seq > 0)
                .sorted((a, b) -> b.compareTo(a))
                .collect(Collectors.toList());
            
            if (!sequences.isEmpty()) {
                nextSequence = sequences.get(0) + 1;
            }
        }
        
        return String.format("%03d-%s", nextSequence, monthYear);
    }

    @Transactional
    public Leads CreateLeadByNewContact(Leads lead, Contacts contact) {
        contact.setId(null);
        Contacts savedContact = contactsService.create(contact);

        lead.setId(null);
        
        if (lead.getStatus() == null) {
            lead.setStatus(LeadStatus.TO_DO);
        }
        
        String leadNumber = generateLeadNumber(lead.getLeadType());
        lead.setLeadNumber(leadNumber);

        LeadsEntity leadEntity = leadMapper.toEntity(lead);

        ContactsEntity contactEntity = contactsRepository.findById(savedContact.getId())
                .orElseThrow(() -> new ContactExceptions.ContactNotFoundException(savedContact.getId()));

        ProjectTypeEntity projectTypeEntity = projectTypeRepository.findById(lead.getProjectType().getId())
                .orElseThrow(() -> new ProjectTypeExceptions.ProjectTypeNotFoundException(lead.getProjectType().getId()));

        leadEntity.setContact(contactEntity);
        leadEntity.setProjectType(projectTypeEntity);

        try {
            LeadsEntity savedLeadEntity = repository.save(leadEntity);
            return leadMapper.toDto(savedLeadEntity);
        } catch (DataIntegrityViolationException e) {
            throw new LeadExceptions.LeadCreationException("Data integrity error creating lead", e);
        }
    }

    @Transactional
    public Leads CreateLeadByExistingContact(Leads lead, Long contactId) {
        lead.setId(null);
        
        if (lead.getStatus() == null) {
            lead.setStatus(LeadStatus.TO_DO);
        }
        
        String leadNumber = generateLeadNumber(lead.getLeadType());
        lead.setLeadNumber(leadNumber);

        LeadsEntity leadEntity = leadMapper.toEntity(lead);

        ContactsEntity contactEntity = contactsRepository.findById(contactId)
                .orElseThrow(() -> new ContactExceptions.ContactNotFoundException(contactId));

        ProjectTypeEntity projectTypeEntity = projectTypeRepository.findById(lead.getProjectType().getId())
                .orElseThrow(() -> new ProjectTypeExceptions.ProjectTypeNotFoundException(lead.getProjectType().getId()));

        leadEntity.setContact(contactEntity);
        leadEntity.setProjectType(projectTypeEntity);

        try {
            LeadsEntity savedLeadEntity = repository.save(leadEntity);
            return leadMapper.toDto(savedLeadEntity);
        } catch (DataIntegrityViolationException e) {
            throw new LeadExceptions.LeadCreationException("Data integrity error creating lead", e);
        }
    }

    @Transactional
    public boolean deleteLead(Long leadId) {
        if (!repository.existsById(leadId)) {
            throw new LeadExceptions.LeadNotFoundException(leadId);
        }
        
        try {
            repository.deleteById(leadId);
            return true;
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Cannot delete lead due to existing references", e);
        }
    }
}
