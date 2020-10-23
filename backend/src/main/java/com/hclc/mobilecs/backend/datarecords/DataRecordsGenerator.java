package com.hclc.mobilecs.backend.datarecords;

import java.time.ZonedDateTime;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.UUID.nameUUIDFromBytes;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

class DataRecordsGenerator {
    private static final long AGREEMENT_ID_SEED = 31423434;
    private static final long INTERNAL_RECORD_ID_SEED = 44134134;
    private static final long RECORDED_BYTES_SEED = 24524555;
    private static final int SECONDS_BETWEEN_RECORDS = 3600;
    private static final long MAX_BYTES_IN_RECORD = 10 * 1024 * 1024; // 10 MB
    private static final ZonedDateTime BEGIN_TIME = ZonedDateTime.parse("2020-01-01T01:00:00+01:00[Europe/Warsaw]");
    private final Random agreementIdRandom = new Random(AGREEMENT_ID_SEED);
    private final int numberOfAgreements;
    private final int numberOfMonths;

    DataRecordsGenerator(int numberOfAgreements, int numberOfMonths) {
        this.numberOfAgreements = numberOfAgreements;
        this.numberOfMonths = numberOfMonths;
    }

    List<DataRecord> generate() {
        return range(0, numberOfAgreements).mapToObj(this::recordsInAgreement).flatMap(List::stream).collect(toList());
    }

    private List<DataRecord> recordsInAgreement(int agreementIndex) {
        Agreement agreement = new Agreement(agreementIndex);
        List<DataRecord> records = new LinkedList<>();
        ZonedDateTime recordedAt = BEGIN_TIME;
        ZonedDateTime endTime = BEGIN_TIME.plusMonths(numberOfMonths).truncatedTo(DAYS);
        while (recordedAt.isBefore(endTime)) {
            records.add(record(recordedAt, agreement));
            recordedAt = recordedAt.plusSeconds(SECONDS_BETWEEN_RECORDS);
        }
        return records;
    }

    private DataRecord record(ZonedDateTime recordedAt, Agreement agreement) {
        return new DataRecord(
                new DataRecordKey(
                        agreement.agreementId,
                        (short) recordedAt.getYear(),
                        (byte) recordedAt.getMonthValue(),
                        Date.from(recordedAt.toInstant()),
                        agreement.nextInternalRecordId()
                ),
                agreement.nextRecordedBytes()
        );
    }

    private class Agreement {
        final UUID agreementId;
        private final Random internalRecordIdRandom;
        private final Random recordedBytesRandom;

        Agreement(int agreementIndex) {
            agreementId = nextUuid(agreementIdRandom);
            internalRecordIdRandom = new Random(INTERNAL_RECORD_ID_SEED + agreementIndex);
            recordedBytesRandom = new Random(RECORDED_BYTES_SEED + agreementIndex);
        }

        UUID nextInternalRecordId() {
            return nextUuid(internalRecordIdRandom);
        }

        long nextRecordedBytes() {
            return (long) (recordedBytesRandom.nextDouble() * MAX_BYTES_IN_RECORD);
        }

        private UUID nextUuid(Random random) {
            byte[] buffer = new byte[8];
            random.nextBytes(buffer);
            return nameUUIDFromBytes(buffer);
        }
    }
}
