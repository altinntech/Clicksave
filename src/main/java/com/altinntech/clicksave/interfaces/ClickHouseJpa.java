package com.altinntech.clicksave.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClickHouseJpa<T> {

    T save(T entity);
    Optional<T> findById(UUID id);
    List<T> findAll();
    void delete(T entity);
    void deleteAll();
}
