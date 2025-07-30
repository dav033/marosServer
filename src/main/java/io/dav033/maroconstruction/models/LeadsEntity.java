package io.dav033.maroconstruction.models;

import io.dav033.maroconstruction.enums.LeadStatus;
import io.dav033.maroconstruction.enums.LeadType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class LeadsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "id", nullable = false)
    private Long id;

    @ToString.Include
    @Column(name = "lead_number", length = 50, nullable = false, unique = true)
    private String leadNumber;

    @ToString.Include
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @ToString.Include
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @ToString.Include
    @Column(name = "location", length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "text")
    private LeadStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name ="lead_type", columnDefinition = "text")
    private LeadType leadType;

    /**
     * Relación con ContactsEntity
     */
    @ManyToOne(fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE },
            optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    private ContactsEntity contact;

    /**
     * Relación con ProjectTypeEntity a través de la columna "type"
     * Se ha eliminado CascadeType.PERSIST para evitar el error de entidad detached.
     */
    @ManyToOne(fetch = FetchType.LAZY,
            cascade = CascadeType.MERGE,
            optional = false)
    @JoinColumn(name = "type", nullable = false)
    private ProjectTypeEntity projectType;

    @Transient
    public Long getContactId() {
        return (contact != null ? contact.getId() : null);
    }

    @Transient
    public Long getProjectTypeId() {
        return (projectType != null ? projectType.getId() : null);
    }
}
