package com.altinntech.clicksave.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The {@code ClickHouseJpa} interface defines common JPA-like methods for interacting with ClickHouse databases.
 * It is parameterized by the entity type {@code T}.
 *
 * <p>Author: Fyodor Plotnikov</p>
 *
 * @param <T> the type parameter representing the entity type
 */
public interface ClickHouseJpa<T, ID> {

    /**
     * Saves the given entity.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    <S extends T> S save(S entity);


    /**
     * Retrieves an entity by its ID.
     *
     * @param id the ID of the entity to retrieve
     * @return an {@code Optional} containing the entity, or empty if not found
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves all entities of type T.
     *
     * @return a list containing all entities
     */
    <S extends T> List<S>  findAll();

    /**
     * Deletes the given entity.
     *
     * @param entity the entity to delete
     */
    void delete(T entity);

    /**
     * Deletes all entities of type T.
     */
    void deleteAll();
}
