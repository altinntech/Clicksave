package com.altinntech.clicksave.core.ids;

import com.altinntech.clicksave.interfaces.IdGenerator;

public enum IdGenerators {
    DEFAULT(new DefaultIdGenerator()),
    INT_INCREMENT(new IncrementalIntegerIdGenerator()),
    LONG_INCREMENT(new IncrementalLongIdGenerator()),
    UUID_V3(new UUIDV3Generator()),
    UUID_V4(new UUIDV4Generator()),
    UUID_V7(new UUIDV7Generator());

    private final IdGenerator<?> actualGenerator;

    IdGenerators(IdGenerator<?> actualGenerator) {
        this.actualGenerator = actualGenerator;
    }

    public IdGenerator<?> getActualGenerator(Class<?> idType) {
        IdGenerator<?> resolved;
        if (actualGenerator instanceof DefaultIdGenerator defaultPlaceHolder) {
            resolved = defaultPlaceHolder.getDefaultGenerator(idType);
        } else {
            resolved = actualGenerator;
        }
        assert idType.equals(resolved.getIdType());
        return resolved;
    }
}
