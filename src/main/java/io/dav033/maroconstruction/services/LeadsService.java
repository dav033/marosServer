package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.dto.Leads;
import io.dav033.maroconstruction.enums.LeadStatus;
import io.dav033.maroconstruction.enums.LeadType;
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

    /**
     * Obtiene todos los números de lead válidos para el tipo especificado
     */
    public List<String> getAllLeadNumbers(LeadType leadType) {
        return repository.findAllLeadNumbersByType(leadType);
    }

    /**
     * Genera el siguiente número de lead para el tipo y mes/año actual
     * Formato: XXX-MMYY (ej: 001-0725)
     */
    private String generateLeadNumber(LeadType leadType) {
        LocalDate now = LocalDate.now();
        String monthYear = now.format(DateTimeFormatter.ofPattern("MMyy"));
        
        // Obtener todos los números de lead válidos
        List<String> allLeadNumbers = getAllLeadNumbers(leadType);
        
        int nextSequence = 1;
        if (!allLeadNumbers.isEmpty()) {
            // Extraer solo la parte numérica (XXX) de cada número de lead
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
                .filter(seq -> seq > 0) // Filtrar valores inválidos
                .sorted((a, b) -> b.compareTo(a)) // Ordenar de mayor a menor
                .collect(Collectors.toList());
            
            // Tomar el mayor número y sumarle 1
            if (!sequences.isEmpty()) {
                nextSequence = sequences.get(0) + 1;
            }
        }
        
        return String.format("%03d-%s", nextSequence, monthYear);
    }

    @Transactional
    public Leads CreateLeadByNewContact(Leads lead, Contacts contact) {
        try {
            // Asegurar que el contacto no tenga ID para que sea tratado como nuevo
            contact.setId(null);
            Contacts savedContact = contactsService.create(contact);

            // Asegurar que el lead no tenga ID para que sea tratado como nuevo
            lead.setId(null);
            
            // Si el status es null, asignar TO_DO automáticamente
            if (lead.getStatus() == null) {
                lead.setStatus(LeadStatus.TO_DO);
            }
            
            // Generar el número de lead automáticamente
            String leadNumber = generateLeadNumber(lead.getLeadType());
            lead.setLeadNumber(leadNumber);

            // Mapear el lead a entidad ANTES de asignar las relaciones
            LeadsEntity leadEntity = leadMapper.toEntity(lead);

            // Obtener la entidad del contacto desde la base de datos usando el ID
            ContactsEntity contactEntity = contactsRepository.findById(savedContact.getId())
                    .orElseThrow(() -> new RuntimeException("Contacto no encontrado después de guardar"));

            // Obtener la entidad del projectType desde la base de datos
            ProjectTypeEntity projectTypeEntity = projectTypeRepository.findById(lead.getProjectType().getId())
                    .orElseThrow(() -> new RuntimeException("Tipo de proyecto no encontrado con ID: " + lead.getProjectType().getId()));

            // Asignar las entidades al lead
            leadEntity.setContact(contactEntity);
            leadEntity.setProjectType(projectTypeEntity);

            LeadsEntity savedLeadEntity = repository.save(leadEntity);

            return leadMapper.toDto(savedLeadEntity);
        } catch (DataIntegrityViolationException e) {
            // Log del error específico para debugging
            System.err.println("Error de integridad de datos al crear lead: " + e.getMessage());

            // Re-lanzar la excepción con un mensaje más claro
            throw new RuntimeException(
                    "Error al crear el lead. Puede ser un problema de secuencia en la base de datos. " +
                            "Por favor, contacta al administrador del sistema.",
                    e);
        }
    }

    @Transactional
    public Leads CreateLeadByExistingContact(Leads lead, Long contactId) {
        try {
            // Asegurar que el lead no tenga ID para que sea tratado como nuevo
            lead.setId(null);
            
            // Si el status es null, asignar TO_DO automáticamente
            if (lead.getStatus() == null) {
                lead.setStatus(LeadStatus.TO_DO);
            }
            
            // Generar el número de lead automáticamente
            String leadNumber = generateLeadNumber(lead.getLeadType());
            lead.setLeadNumber(leadNumber);

            // Mapear el lead a entidad ANTES de asignar las relaciones
            LeadsEntity leadEntity = leadMapper.toEntity(lead);

            // Obtener la entidad del contacto existente desde la base de datos
            ContactsEntity contactEntity = contactsRepository.findById(contactId)
                    .orElseThrow(() -> new RuntimeException("Contacto no encontrado con ID: " + contactId));

            // Obtener la entidad del projectType desde la base de datos
            ProjectTypeEntity projectTypeEntity = projectTypeRepository.findById(lead.getProjectType().getId())
                    .orElseThrow(() -> new RuntimeException("Tipo de proyecto no encontrado con ID: " + lead.getProjectType().getId()));

            // Asignar las entidades al lead
            leadEntity.setContact(contactEntity);
            leadEntity.setProjectType(projectTypeEntity);

            LeadsEntity savedLeadEntity = repository.save(leadEntity);

            return leadMapper.toDto(savedLeadEntity);
        } catch (DataIntegrityViolationException e) {
            // Log del error específico para debugging
            System.err.println("Error de integridad de datos al crear lead: " + e.getMessage());

            // Re-lanzar la excepción con un mensaje más claro
            throw new RuntimeException(
                    "Error al crear el lead. Puede ser un problema de secuencia en la base de datos. " +
                            "Por favor, contacta al administrador del sistema.",
                    e);
        }
    }

    /**
     * Elimina un lead por su ID
     * @param leadId ID del lead a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    @Transactional
    public boolean deleteLead(Long leadId) {
        try {
            if (repository.existsById(leadId)) {
                repository.deleteById(leadId);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error al eliminar lead con ID " + leadId + ": " + e.getMessage());
            throw new RuntimeException("Error al eliminar el lead", e);
        }
    }
}
