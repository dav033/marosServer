package io.dav033.maroconstruction.models;

import io.dav033.maroconstruction.enums.InvoiceStatus;
import io.dav033.maroconstruction.enums.ProjectStatus;
import io.hypersistence.utils.hibernate.type.array.FloatArrayType;
import jakarta.persistence.*;
import lombok.*;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
            columnDefinition = "numeric[]"      // ó DECIMAL[] en PostgreSQL
    )
    private List<BigDecimal> payments;

    @Column(name = "project_status")
    private ProjectStatus projectStatus;

    @Column(name = "invoice_status")
    private InvoiceStatus invoiceStatus;

    @Column(name = "quickbooks")
    private Boolean quickbooks;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    /**
     * Lead asignado al proyecto
     */
    @ManyToOne(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            optional = false)
    @JoinColumn(name = "lead_id", nullable = false)
    private LeadsEntity lead;

    /**
     * Para exponer el ID del lead sin inicializar la proxy
     */
    @Transient
    public Long getLeadId() {
        return (lead != null ? lead.getId() : null);
    }
}
