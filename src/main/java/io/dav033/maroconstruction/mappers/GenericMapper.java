package io.dav033.maroconstruction.mappers;

import java.util.List;

import org.mapstruct.MappingTarget;

public interface GenericMapper<T, E> {
    E toEntity(T dto);
    T toDto(E entity);
    List<T> toDtoList(List<E> entityList);
    void updateEntity(T dto, @MappingTarget E entity);}

