package io.dav033.maroconstruction.services.base;

import java.util.List;

public interface CrudService<T, ID> {
    T create(T dto);

    List<T> findAll();

    T findById(ID id);

    T update(ID id, T dto);

    void delete(ID id);

    List<T> saveAll(List<T> dtos);
}