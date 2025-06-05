package com.altinntech.clicksave;

import com.altinntech.clicksave.core.CSBootstrap;
import com.altinntech.clicksave.core.ClicksaveInternalRepository;
import com.altinntech.clicksave.core.ThreadPoolManager;
import com.altinntech.clicksave.core.dto.SimpleQueryInfo;
import com.altinntech.clicksave.core.query.executor.QueryExecutor;
import com.altinntech.clicksave.interfaces.ClickHouseJpa;
import com.altinntech.clicksave.interfaces.QueryInfo;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

public abstract class AbstractClicksaveRepository<T, ID> implements ClickHouseJpa<T, ID> {

    protected final Class<T> entityType;
    protected final Class<ID> idType;
    protected final QueryExecutor queryExecutor;

    private final ClicksaveInternalRepository repository;
    private final ThreadPoolManager threadPoolManager;

    public AbstractClicksaveRepository(CSBootstrap bootstrap, Class<T> entityType, Class<ID> idType) {
        this.entityType = entityType;
        this.idType = idType;
        this.repository = bootstrap.getRepository();
        this.queryExecutor = bootstrap.getQueryExecutor();
        this.threadPoolManager = bootstrap.getThreadPoolManager();
    }

    @SneakyThrows
    @Override
    public <S extends T> S save(S entity) {
        return repository.save(entity, entity.getClass());
    }

    @SneakyThrows
    @Override
    public <S extends T> Future<S> saveAsync(S entity) {
        return threadPoolManager.saveAsync(new Object[]{entity}, idType, repository);
    }

    @Override
    @SneakyThrows
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(repository.findById(entityType, id));
    }

    @Override
    @SneakyThrows
    public long count() {
        return repository.count(entityType);
    }

    @Override
    @SneakyThrows
    public <S extends T> List<S> findAll() {
        return repository.findAll(entityType).stream().map(t -> (S) t).toList();
    }

    @Override
    @SneakyThrows
    public void delete(T entity) {
        repository.delete(entity);
    }

    @Override
    @SneakyThrows
    public void deleteAll() {
        repository.deleteAll(entityType);
    }

    @SneakyThrows
    @Override
    public <R> List<R> findAllCustomQuery(Class<R> producer, String query, List params) {
        return (List<R>) queryExecutor.processQuery(new SimpleQueryInfo(
                "findAllCustomQuery", producer, entityType, List.class, query, params, false
        ));
    }

    @SneakyThrows
    @Override
    public <R> Optional<R> findSingleCustomQuery(Class<R> producer, String query, List params) {
        return (Optional<R>) queryExecutor.processQuery(new SimpleQueryInfo(
                "findSingleCustomQuery", producer, entityType, Optional.class, query, params, false
        ));
    }

    @SneakyThrows
    protected <E> E execute(QueryInfo queryInfo) {
        return (E) queryExecutor.processQuery(queryInfo);
    }

    @Override
    @SneakyThrows
    public void saveBatch() {
        repository.saveBatch(entityType);
    }
}
