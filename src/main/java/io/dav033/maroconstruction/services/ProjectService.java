package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.Projects;
import io.dav033.maroconstruction.models.ContactsEntity;
import io.dav033.maroconstruction.models.LeadsEntity;
import io.dav033.maroconstruction.models.ProjectEntity;
import io.dav033.maroconstruction.repositories.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ProjectService {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProjectService.class);

  private final ProjectRepository projectRepository;

  public ProjectService(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  @Transactional(readOnly = true)
  public List<Projects> getProjectsWithLead() {
    var projects = projectRepository.findProjectsWithLeadAndContact();
    return projects.stream().map(this::convertToDtoWithLead).toList();
  }
  private static LocalDate toLocalDate(Date d) {
    return d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }

  private Projects convertToDtoWithLead(ProjectEntity entity) {
    var builder = Projects.builder()
        .id(entity.getId())
        .projectName(entity.getProjectName())
        .overview(entity.getOverview())
        .payments(entity.getPayments())
        .projectStatus(entity.getProjectStatus())
        .invoiceStatus(entity.getInvoiceStatus())
        .quickbooks(entity.getQuickbooks())
        .startDate(toLocalDate(entity.getStartDate())) 
        .endDate(toLocalDate(entity.getEndDate()));    

    LeadsEntity lead = entity.getLead();
    if (lead != null) {
      builder
          .leadId(lead.getId())
          .leadName(lead.getName())
          .leadNumber(lead.getLeadNumber());
      String location = null;
      try { location = lead.getLocation(); } catch (Exception ignored) {}
      if (location == null || location.isBlank()) location = lead.getName();
      builder.location(location);
      try {
        ContactsEntity c = lead.getContact();
        if (c != null) {
          builder.contactName(c.getName());
          builder.customerName(c.getCompanyName()); 
        }
      } catch (Exception e) {
        log.debug("Contact not loaded for lead {}", lead.getId());
      }
    }
    return builder.build();
  }
}
