package io.dav033.maroconstruction.models;

import io.dav033.maroconstruction.enums.InvoiceStatus;
import io.dav033.maroconstruction.enums.ProjectStatus;
import jakarta.persistence.*;
import jakarta.persistence.EnumType;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "projects")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "project_name", length = 100, nullable = false)
    private String projectName;

    @Column(name = "overview", columnDefinition = "text")
    private String overview;

    @Type(ListArrayType.class)
    @Column(
            name = "payments",
            columnDefinition = "numeric[]"      
    )
    private List<BigDecimal> payments;

    @Column(name = "project_status")
    @Enumerated(EnumType.STRING)
    private ProjectStatus projectStatus;

    @Column(name = "invoice_status")
    @Enumerated(EnumType.STRING)
    private InvoiceStatus invoiceStatus;

    @Column(name = "quickbooks")
    private Boolean quickbooks;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

        @ManyToOne(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            optional = true)
    @JoinColumn(name = "lead_id", nullable = true)
    private LeadsEntity lead;

        @Transient
    public Long getLeadId() {
        return (lead != null ? lead.getId() : null);
    }

    public ProjectEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LeadsEntity getLead() { return lead; }
    public void setLead(LeadsEntity lead) { this.lead = lead; }
}
