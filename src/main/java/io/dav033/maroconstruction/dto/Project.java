package io.dav033.maroconstruction.dto;

import io.dav033.maroconstruction.enums.InvoiceStatus;
import io.dav033.maroconstruction.enums.ProjectStatus;

import java.time.LocalDate;
import java.util.List;

public class Project {
    private Long id;
    private String projectName;
    private String overview;
    private List<Float> payments;
    private ProjectStatus projectStatus;
    private InvoiceStatus invoiceStatus;
    private Boolean quickbooks;
    private LocalDate startDate;
    private LocalDate endDate;
    private Leads lead;

    public Project() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    public List<Float> getPayments() { return payments; }
    public void setPayments(List<Float> payments) { this.payments = payments; }
    public ProjectStatus getProjectStatus() { return projectStatus; }
    public void setProjectStatus(ProjectStatus projectStatus) { this.projectStatus = projectStatus; }
    public InvoiceStatus getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(InvoiceStatus invoiceStatus) { this.invoiceStatus = invoiceStatus; }
    public Boolean getQuickbooks() { return quickbooks; }
    public void setQuickbooks(Boolean quickbooks) { this.quickbooks = quickbooks; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Leads getLead() { return lead; }
    public void setLead(Leads lead) { this.lead = lead; }
}
