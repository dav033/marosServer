package io.dav033.maroconstruction.dto.requests;

import io.dav033.maroconstruction.enums.InvoiceStatus;
import io.dav033.maroconstruction.enums.ProjectStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class CreateProjectRequest {
    
    private String projectName;
    private String overview;
    private List<BigDecimal> payments;
    private ProjectStatus projectStatus;
    private InvoiceStatus invoiceStatus;
    private Boolean quickbooks;
    private Date startDate;
    private Date endDate;
    private Long leadId;

    public CreateProjectRequest() {}
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    public List<BigDecimal> getPayments() { return payments; }
    public void setPayments(List<BigDecimal> payments) { this.payments = payments; }
    public ProjectStatus getProjectStatus() { return projectStatus; }
    public void setProjectStatus(ProjectStatus projectStatus) { this.projectStatus = projectStatus; }
    public InvoiceStatus getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(InvoiceStatus invoiceStatus) { this.invoiceStatus = invoiceStatus; }
    public Boolean getQuickbooks() { return quickbooks; }
    public void setQuickbooks(Boolean quickbooks) { this.quickbooks = quickbooks; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }
}