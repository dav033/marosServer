package io.dav033.maroconstruction.models;

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

    // LocalDateAttributeConverter se aplica automáticamente
    @ToString.Include
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @ToString.Include
    @Column(name = "location", length = 255)
    private String location;

    @ToString.Include
    @Column(name = "status", columnDefinition = "text")
    private String status;

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
     */
    @ManyToOne(fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE },
            optional = false)
    @JoinColumn(name = "type", nullable = false)
    private ProjectTypeEntity projectType;

    /**
     * Para exponer el ID de contacto sin inicializar la proxy
     */
    @Transient
    public Long getContactId() {
        return (contact != null ? contact.getId() : null);
    }

    /**
     * Para exponer el ID de tipo de proyecto sin inicializar la proxy
     */
    @Transient
    public Long getProjectTypeId() {
        return (projectType != null ? projectType.getId() : null);
    }
}
