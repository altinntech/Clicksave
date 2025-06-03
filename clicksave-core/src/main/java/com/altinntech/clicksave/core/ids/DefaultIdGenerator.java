package com.altinntech.clicksave.core.ids;

import com.altinntech.clicksave.exceptions.IdGeneratorNotFoundException;
import com.altinntech.clicksave.exceptions.InvalidIdGenerator;
import com.altinntech.clicksave.interfaces.IdGenerator;

import java.util.Set;

public class DefaultIdGenerator implements IdGenerator<Object> {

    private final Set<IdGenerator<?>> defaultGenerators = Set.of(
            new IncrementalIntegerIdGenerator(),
            new IncrementalLongIdGenerator(),
            new UUIDV4Generator()
    );

    public DefaultIdGenerator() {

    }

    @Override
    public Class<Object> getIdType() {
        throw new InvalidIdGenerator();
    }

    @Override
    public boolean isPrevIdAware() {
        throw new InvalidIdGenerator();
    }

    @Override
    public Object generateNextId(Object lastId) {
        throw new InvalidIdGenerator();
    }

    @Override
    public Object generateNextId() {
        throw new InvalidIdGenerator();
    }

    public <T> IdGenerator<T> getDefaultGenerator(Class<T> type) {
        for (IdGenerator<?> generator : defaultGenerators) {
            if (generator.getIdType().equals(type)) {
                return (IdGenerator<T>) generator;
            }
        }
        throw new IdGeneratorNotFoundException(type);
    }
}
