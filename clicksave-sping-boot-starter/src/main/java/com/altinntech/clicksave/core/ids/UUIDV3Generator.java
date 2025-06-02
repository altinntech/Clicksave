package com.altinntech.clicksave.core.ids;

import com.altinntech.clicksave.exceptions.InvalidIdGenerator;
import com.altinntech.clicksave.interfaces.IdGenerator;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDV3Generator implements IdGenerator<UUID> {

    @Override
    public Class<UUID> getIdType() {
        return UUID.class;
    }

    @Override
    public boolean isPrevIdAware() {
        return true;
    }

    @Override
    public UUID generateNextId(UUID lastId) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(lastId.getMostSignificantBits());
        bb.putLong(lastId.getLeastSignificantBits());
        return UUID.nameUUIDFromBytes(bb.array());
    }

    @Override
    public UUID generateNextId() {
        throw new InvalidIdGenerator();
    }
}
