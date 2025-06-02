package com.altinntech.clicksave.core.ids;

import com.altinntech.clicksave.core.CSUtils;
import com.altinntech.clicksave.enums.IDTypes;
import com.altinntech.clicksave.exceptions.InvalidIdGenerator;
import com.altinntech.clicksave.interfaces.IdGenerator;

public class IncrementalIntegerIdGenerator implements IdGenerator<Integer> {

    @Override
    public Class<Integer> getIdType() {
        return Integer.class;
    }

    @Override
    public Integer generateNextId(Integer lastId) {
        return CSUtils.generateIntegerId(lastId);
    }

    @Override
    public boolean isPrevIdAware() {
        return true;
    }

    @Override
    public Integer generateNextId() {
        throw new InvalidIdGenerator();
    }
}
