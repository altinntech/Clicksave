package com.altinntech.clicksave.core.ids;

import com.altinntech.clicksave.exceptions.InvalidIdGenerator;
import com.altinntech.clicksave.interfaces.IdGenerator;

import java.util.UUID;

public class UUIDV4Generator implements IdGenerator<UUID> {

    @Override
    public Class<UUID> getIdType() {
        return UUID.class;
    }

    @Override
    public boolean isPrevIdAware() {
        return false;
    }

    @Override
    public UUID generateNextId(UUID lastId) {
        throw new InvalidIdGenerator();
    }

    @Override
    public UUID generateNextId() {
        return UUID.randomUUID();
    }
}
