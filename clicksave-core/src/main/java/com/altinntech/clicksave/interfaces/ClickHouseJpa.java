package com.altinntech.clicksave.interfaces;

import com.altinntech.clicksave.annotations.SettableQuery;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * The {@code ClickHouseJpa} interface defines common JPA-like methods for interacting with ClickHouse databases.
 * It is parameterized by the entity type {@code T}.
 *
 * @author Fyodor Plotnikov
 *
 * @param <T> the type parameter representing the entity type
 */
public interface ClickHouseJpa<T, ID> extends ClicksaveUtils {

    /**
     * Saves the given entity.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    <S extends T> S save(S entity);

    /**
     * Saves async the given entity.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    <S extends T> Future<S> saveAsync(S entity);


    /**
     * Retrieves an entity by its ID.
     *
     * @param id the ID of the entity to retrieve
     * @return an {@code Optional} containing the entity, or empty if not found
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves a count of entities.
     *
     * @return count of entities
     */
    long count();

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

    /**
     * Performs a custom query and returns the result.
     *
     * @param query the custom query to perform
     * @return the result of the custom query
     */
    @SettableQuery
    <R> List<R> findAllCustomQuery(Class<R> producer, String query, Object... params);
}
