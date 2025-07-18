package io.dav033.maroconstruction.repositories;

import io.dav033.maroconstruction.models.ContactsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContactsRepository extends JpaRepository<ContactsEntity, Long> {
    Optional<ContactsEntity> findByName(@Param("name") String name);
}
