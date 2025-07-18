package io.dav033.maroconstruction.services.base;

import io.dav033.maroconstruction.mappers.GenericMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor(force = true)
public abstract class BaseService<T, ID, E, R extends JpaRepository<E, ID>> implements CrudService<T, ID> {
    protected final R repository;
    protected final GenericMapper<T, E> mapper;

//    public BaseService(R repository, GenericMapper<T, E> mapper) {
//        this.repository = repository;
//        this.mapper = mapper;
//    }

    @Override
    public T create(T dto) {
        E entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @Override
    public List<T> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(ID id) {
        repository.deleteById(id);
    }

    @Override
    public T findById(ID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public T update(ID id, T dto) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entidad no encontrada con id " + id));
        // Actualiza la entidad existente utilizando el mapper genérico
        mapper.updateEntity(dto, entity);
        return mapper.toDto(repository.save(entity));
    }



    @Override
    public List<T> saveAll(List<T> dtos) {
        List<E> entities = dtos.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
        repository.saveAll(entities);
        return null;
    }
}
