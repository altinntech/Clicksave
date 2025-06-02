package com.altinntech.clicksave.core.ids;

import com.altinntech.clicksave.exceptions.InvalidIdGenerator;
import com.altinntech.clicksave.interfaces.IdGenerator;
import lombok.SneakyThrows;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

public class UUIDV7Generator implements IdGenerator<UUID> {

    private static final SecureRandom secureRandom = new SecureRandom();
    
    // UUIDv7 layout (128 bits):
    // 16 octets: ff ff ff | f | f ff | (3 | 3) f ff ff ff ff ff ff ff ff ff ff
    // [48 bits timestamp][4 bits version][12 bits rand_a][2 bits variant][62 bits rand_b]
    private static final long TIMESTAMP_MASK = 0xFFFFFFFFFFFF0000L;
    private static final long VERSION_MASK = 0x000000000000F000L;
    private static final long RAND_A_MASK = 0x0000000000000FFFL;
    private static final long VARIANT_MASK = 0xC000000000000000L;
    private static final long VARIANT_10 = 0x8000000000000000L;

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
        long currentTimeMillis = Instant.now().toEpochMilli();

        long msb = (currentTimeMillis & TIMESTAMP_MASK) << 16; // 48 bits timestamp
        msb |= (0x7L << 12); // Set version '7' (4 bits)
        msb |= (secureRandom.nextInt() & VERSION_MASK); // Fill rand_a

        long lsb = (((long) secureRandom.nextInt()) << 32) | (secureRandom.nextInt() & 0x00000000FFFFFFFFL); // Fill rand_b
        lsb &= ~(VARIANT_MASK); // Clear variant bits
        lsb |= VARIANT_10; // Set variant '10' (RFC 4122)

        return new UUID(msb, lsb);
    }
}
