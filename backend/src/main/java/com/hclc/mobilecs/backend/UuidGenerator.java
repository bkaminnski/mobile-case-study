package com.hclc.mobilecs.backend;

import java.util.Random;
import java.util.UUID;

import static java.util.UUID.nameUUIDFromBytes;

public class UuidGenerator {
    public static UUID nextUuid(Random random) {
        byte[] buffer = new byte[8];
        random.nextBytes(buffer);
        return nameUUIDFromBytes(buffer);
    }
}
