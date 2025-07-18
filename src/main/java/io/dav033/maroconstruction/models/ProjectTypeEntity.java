package io.dav033.maroconstruction.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "columns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Si la columna realmente se llama "Name" con mayúscula,
     * hay que indicarlo en el @Column;
     * de lo contrario se mapeará automáticamente a "name".
     */
    @Column(name = "name", columnDefinition = "text")
    private String name;

}
