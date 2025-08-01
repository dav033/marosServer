package io.dav033.maroconstruction.services.base;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import io.dav033.maroconstruction.mappers.GenericMapper;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class BaseService<
        D,               // DTO
        ID,              // Primary key type
        E,               // JPA Entity
        R extends JpaRepository<E, ID>  // Repository
        > implements CrudService<D, ID> {

    protected final R repository;
    protected final GenericMapper<D, E> mapper;

    @Transactional
    @Override
    public D create(D dto) {
        E entity = mapper.toEntity(dto);
        E saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<D> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public D findById(ID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() ->
                        new EntityNotFoundException("Entity not found with id " + id)
                );
    }

    @Transactional
    @Override
    public D update(ID id, D dto) {
        E entity = repository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Entity not found with id " + id)
                );
        mapper.updateEntity(dto, entity);
        E saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Transactional
    @Override
    public void delete(ID id) {
        repository.deleteById(id);
    }

    @Transactional
    @Override
    public List<D> saveAll(List<D> dtos) {
        List<E> entities = dtos.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
        List<E> savedEntities = repository.saveAll(entities);
        return savedEntities.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
