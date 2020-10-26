package com.hclc.mobilecs.backend.incomingdatarecords;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.UUID.nameUUIDFromBytes;
import static java.util.stream.Collectors.toList;

class IncomingDataRecordsGenerator {
    private static final long MSISDN_SEED = 84744291;
    private static final long MSISDNS_SHUFFLING_SEED = 7371661;
    private static final long ID_SEED = 33774923;
    private static final long RECORDED_BYTES_SEED = 24524555;
    private static final int SECONDS_BETWEEN_RECORDS = 3600;
    private static final long MAX_BYTES_IN_RECORD = 10 * 1024 * 1024; // 10 MB
    private final Random msisdnRandom = new Random(MSISDN_SEED);
    private final Random msisdnsShufflingRandom = new Random(MSISDNS_SHUFFLING_SEED);
    private final int numberOfMonths;
    private final List<Msisdn> msisdns;
    private ZonedDateTime beginTime = ZonedDateTime.parse("2020-01-01T01:00:00+01:00[Europe/Warsaw]");

    IncomingDataRecordsGenerator(int numberOfMsisdns, int numberOfMonths) {
        this.msisdns = generateMsidns(numberOfMsisdns);
        this.numberOfMonths = numberOfMonths;
    }

    private List<Msisdn> generateMsidns(int numberOfMsisdns) {
        return IntStream.range(0, numberOfMsisdns).mapToObj(Msisdn::new).collect(toList());
    }

    void generateTo(Consumer<IncomingDataRecord> recordConsumer) {
        ZonedDateTime recordedAt = beginTime;
        ZonedDateTime endDate = beginTime.plusMonths(numberOfMonths);
        while (recordedAt.isBefore(endDate)) {
            generateAtTimestamp(recordedAt, recordConsumer);
            recordedAt = recordedAt.plusSeconds(SECONDS_BETWEEN_RECORDS);
        }
        beginTime = recordedAt;
    }

    private void generateAtTimestamp(ZonedDateTime recordedAt, Consumer<IncomingDataRecord> recordConsumer) {
        Collections.shuffle(msisdns, msisdnsShufflingRandom);
        for (Msisdn msisdn : msisdns) {
            IncomingDataRecord incomingDataRecord = new IncomingDataRecord(msisdn.nextId(), recordedAt, msisdn.value, msisdn.nextRecordedBytes());
            recordConsumer.accept(incomingDataRecord);
        }
    }

    private class Msisdn {
        private final String value;
        private final Random idRandom;
        private final Random recordedBytesRandom;

        Msisdn(int index) {
            this.value = nextValue();
            this.idRandom = new Random(ID_SEED + index);
            this.recordedBytesRandom = new Random(RECORDED_BYTES_SEED + index);
        }

        private String nextValue() {
            return "48" + (long) (msisdnRandom.nextDouble() * 1000_000_000L);
        }

        String nextId() {
            byte[] buffer = new byte[8];
            idRandom.nextBytes(buffer);
            return nameUUIDFromBytes(buffer).toString();
        }

        long nextRecordedBytes() {
            return (long) (recordedBytesRandom.nextDouble() * MAX_BYTES_IN_RECORD);
        }
    }
}
