package com.hclc.mobilecs.backend.datarecords;

import java.time.ZonedDateTime;
import java.util.*;

import static com.hclc.mobilecs.backend.UuidGenerator.nextUuid;
import static com.hclc.mobilecs.backend.agreements.AgreementsGenerator.AGREEMENT_ID_SEED;
import static com.hclc.mobilecs.backend.agreements.AgreementsGenerator.SERVICE_START_AT;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

class DataRecordsGenerator {
    private static final long INTERNAL_RECORD_ID_SEED = 44134134;
    private static final long RECORDED_BYTES_SEED = 24524555;
    private static final int SECONDS_BETWEEN_RECORDS = 3600 * 24; // 24 hr
    private static final long MAX_BYTES_IN_RECORD = 10 * 1024 * 1024; // 10 MB
    private final int numberOfAgreements;
    private final int numberOfMonths;
    private Random agreementIdRandom;
    private ZonedDateTime beginTime = SERVICE_START_AT.plusSeconds(SECONDS_BETWEEN_RECORDS);

    DataRecordsGenerator(int numberOfAgreements, int numberOfMonths) {
        this.numberOfAgreements = numberOfAgreements;
        this.numberOfMonths = numberOfMonths;
    }

    List<DataRecord> generate() {
        this.agreementIdRandom = new Random(AGREEMENT_ID_SEED);
        return range(0, numberOfAgreements).mapToObj(this::recordsInAgreement).flatMap(List::stream).collect(toList());
    }

    private List<DataRecord> recordsInAgreement(int agreementIndex) {
        Agreement agreement = new Agreement(agreementIndex);
        List<DataRecord> records = new LinkedList<>();
        ZonedDateTime recordedAt = beginTime;
        ZonedDateTime endTime = beginTime.plusMonths(numberOfMonths);
        while (recordedAt.isBefore(endTime)) {
            records.add(record(recordedAt, agreement));
            recordedAt = recordedAt.plusSeconds(SECONDS_BETWEEN_RECORDS);
        }
        beginTime = recordedAt;
        return records;
    }

    private DataRecord record(ZonedDateTime recordedAt, Agreement agreement) {
        return new DataRecord(
                new DataRecordKey(
                        agreement.agreementId,
                        (short) recordedAt.getYear(),
                        (byte) recordedAt.minusSeconds(1).getMonthValue(),
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
    }
}
