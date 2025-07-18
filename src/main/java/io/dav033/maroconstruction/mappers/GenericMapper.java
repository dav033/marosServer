package io.dav033.maroconstruction.mappers;

import org.mapstruct.MappingTarget;

import java.util.List;

public interface GenericMapper<T, E> {
    E toEntity(T dto);
    T toDto(E entity);
    List<T> toDtoList(List<E> entityList);
    void updateEntity(T dto, @MappingTarget E entity);
}
