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
        ID,              // Tipo de la clave primaria
        E,               // Entidad JPA
        R extends JpaRepository<E, ID>  // Repositorio
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
                        new EntityNotFoundException("Entidad no encontrada con id " + id)
                );
    }

    @Transactional
    @Override
    public D update(ID id, D dto) {
        E entity = repository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Entidad no encontrada con id " + id)
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
