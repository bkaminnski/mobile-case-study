package com.hclc.mobilecs.backend.incomingdatarecords;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.UUID.nameUUIDFromBytes;

class IncomingDataRecordsFilesCreator {
    private static final int FILE_NAME_SEED = 3424233;
    private final Random fileNameRandom = new Random(FILE_NAME_SEED);
    private final AtomicInteger counter = new AtomicInteger(0);
    private final Path dir;

    IncomingDataRecordsFilesCreator(String dir) {
        this.dir = Path.of(dir);
        try {
            Files.createDirectories(this.dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    IncomingDataRecordsFile next() {
        String prefix = String.format("idr_%03d_", counter.getAndIncrement());
        String suffix = String.format("_%s.csv", fileNameUuid());
        return new IncomingDataRecordsFile(dir, prefix, suffix);
    }

    private UUID fileNameUuid() {
        byte[] buffer = new byte[8];
        fileNameRandom.nextBytes(buffer);
        return nameUUIDFromBytes(buffer);
    }
}
