package io.dav033.maroconstruction.dto;

import io.dav033.maroconstruction.enums.InvoiceStatus;
import io.dav033.maroconstruction.enums.ProjectStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Projects {
    private Long id;
    private String projectName;
    private String overview;
    private List<BigDecimal> payments;
    private ProjectStatus projectStatus;
    private InvoiceStatus invoiceStatus;
    private Boolean quickbooks;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long leadId;
    private String leadName;
    private String leadNumber;
    private String location;
    private String contactName;
    private String customerName;

    public Projects() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Projects p = new Projects();
        public Builder id(Long v) { p.id = v; return this; }
        public Builder projectName(String v) { p.projectName = v; return this; }
        public Builder overview(String v) { p.overview = v; return this; }
        public Builder payments(List<BigDecimal> v) { p.payments = v; return this; }
        public Builder projectStatus(ProjectStatus v) { p.projectStatus = v; return this; }
        public Builder invoiceStatus(InvoiceStatus v) { p.invoiceStatus = v; return this; }
        public Builder quickbooks(Boolean v) { p.quickbooks = v; return this; }
        public Builder startDate(LocalDate v) { p.startDate = v; return this; }
        public Builder endDate(LocalDate v) { p.endDate = v; return this; }
        public Builder leadId(Long v) { p.leadId = v; return this; }
        public Builder leadName(String v) { p.leadName = v; return this; }
        public Builder leadNumber(String v) { p.leadNumber = v; return this; }
        public Builder location(String v) { p.location = v; return this; }
        public Builder contactName(String v) { p.contactName = v; return this; }
        public Builder customerName(String v) { p.customerName = v; return this; }
        public Projects build() { return p; }
    }

    public Long getId() { return id; }
    public String getProjectName() { return projectName; }
    public String getOverview() { return overview; }
    public List<BigDecimal> getPayments() { return payments; }
    public ProjectStatus getProjectStatus() { return projectStatus; }
    public InvoiceStatus getInvoiceStatus() { return invoiceStatus; }
    public Boolean getQuickbooks() { return quickbooks; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Long getLeadId() { return leadId; }
    public String getLeadName() { return leadName; }
    public String getLeadNumber() { return leadNumber; }
    public String getLocation() { return location; }
    public String getContactName() { return contactName; }
    public String getCustomerName() { return customerName; }
}
