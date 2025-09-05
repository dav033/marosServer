// ContactsEntity.java
package io.dav033.maroconstruction.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "contacts",
    indexes = {
        @Index(name = "ux_contacts_email_ci", columnList = "email"),
        @Index(name = "ux_contacts_phone", columnList = "phone"),
        @Index(name = "ux_contacts_name_ci", columnList = "name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class ContactsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "id", nullable = false)
    private Long id;

    @ToString.Include
    @Column(name = "company_name", length = 100, nullable = false)
    private String companyName;

    @ToString.Include
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "product", length = 100)
    private String product;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 255)
    private String address;

    // Se aplica automáticamente LocalDateTimeAttributeConverter
    @Column(name = "last_contact")
    private LocalDateTime lastContact;
}
