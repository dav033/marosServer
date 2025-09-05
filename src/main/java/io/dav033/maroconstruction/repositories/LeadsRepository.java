package io.dav033.maroconstruction.repositories;

import io.dav033.maroconstruction.enums.LeadType;
import io.dav033.maroconstruction.models.LeadsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeadsRepository extends JpaRepository<LeadsEntity, Long> {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Consultas existentes
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    @Query("SELECT l FROM LeadsEntity l LEFT JOIN FETCH l.contact LEFT JOIN FETCH l.projectType")
    List<LeadsEntity> findAll();

    @Query("SELECT l FROM LeadsEntity l LEFT JOIN FETCH l.contact LEFT JOIN FETCH l.projectType WHERE l.leadType = :type")
    List<LeadsEntity> findByLeadType(@Param("type") LeadType type);

    @Query("""
            SELECT l.leadNumber
            FROM LeadsEntity l
            WHERE l.leadType = :leadType
              AND l.leadNumber IS NOT NULL
              AND l.leadNumber <> ''
            """)
    List<String> findAllLeadNumbersByType(@Param("leadType") LeadType leadType);

        boolean existsByLeadNumber(String leadNumber);

  boolean existsByLeadNumberAndIdNot(String leadNumber, Long id);

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Nuevo método para generar la secuencia mensual del lead_number
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    @Query("""
            SELECT MAX(
                CAST(SUBSTRING(l.leadNumber, 1, 3) AS integer)
            )
            FROM LeadsEntity l
            WHERE l.leadType = :leadType
              AND FUNCTION('right', l.leadNumber, 4) = :monthYear
            """)
    Optional<Integer> findMaxSequenceForMonth(@Param("leadType") LeadType leadType,
            @Param("monthYear") String monthYear);
}
