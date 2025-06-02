package com.altinntech.clicksave.core.ids;

import com.altinntech.clicksave.core.CSUtils;
import com.altinntech.clicksave.enums.IDTypes;
import com.altinntech.clicksave.exceptions.InvalidIdGenerator;
import com.altinntech.clicksave.interfaces.IdGenerator;

public class IncrementalLongIdGenerator implements IdGenerator<Long> {

    @Override
    public Class<Long> getIdType() {
        return Long.class;
    }

    @Override
    public boolean isPrevIdAware() {
        return true;
    }

    @Override
    public Long generateNextId(Long lastId) {
        return CSUtils.generateLongId(lastId);
    }

    @Override
    public Long generateNextId() {
        throw new InvalidIdGenerator();
    }
}
