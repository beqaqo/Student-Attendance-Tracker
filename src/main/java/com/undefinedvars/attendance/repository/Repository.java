package com.undefinedvars.attendance.repository;

import java.util.List;
import java.util.Optional;
/*
    Repository is a generic interface that defines basic CRUD operations for any entity type T with an identifier of type ID.
    Dependency Inversion point: service depends on abstraction, but implementation.
 */
public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean delete(ID id);
}
