package com.hclc.mobilecs.backend.datarecords;

import com.hclc.mobilecs.backend.agreements.Agreement;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Table
class DataRecord {
    @PrimaryKey
    private final DataRecordKey key;
    private final Long recordedBytes;

    DataRecord(DataRecordKey key, Long recordedBytes) {
        this.key = key;
        this.recordedBytes = recordedBytes;
    }

    static DataRecord from(Agreement agreement, DataRecordUpsertRequest upsertRequest) {
        ZonedDateTime recordedAt = upsertRequest.getRecordedAt().toInstant().atZone(ZoneId.of(agreement.getBillingPeriodTimeZone()));
        DataRecordKey key = new DataRecordKey(
                agreement.getId(),
                (short) recordedAt.minusSeconds(1).getYear(),
                (byte) recordedAt.minusSeconds(1).getMonth().getValue(),
                upsertRequest.getRecordedAt(),
                UUID.fromString(upsertRequest.getInternalRecordId())
        );
        return new DataRecord(key, upsertRequest.getRecordedBytes());
    }

    public DataRecordKey getKey() {
        return key;
    }

    public Long getRecordedBytes() {
        return recordedBytes;
    }

    @Override
    public String toString() {
        return "DataRecord{" +
                "key=" + key +
                ", recordedBytes=" + recordedBytes +
                '}';
    }
}
