package com.hclc.mobilecs.flink;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "mobilecs", name = "data_record")
class DataRecord {
    @Column(name = "agreement_id")
    private final UUID agreementId;
    @Column(name = "year")
    private final short year;
    @Column(name = "month")
    private final byte month;
    @Column(name = "recorded_at")
    private final Date recordedAt;
    @Column(name = "internal_record_id")
    private final UUID internalRecordId;
    @Column(name = "recorded_bytes")
    private final Long recordedBytes;
    @Transient
    private final long maxBytesInBillingPeriod;

    DataRecord(Agreement agreement, EnrichedIncomingDataRecord enrichedIncomingDataRecord) {
        this.agreementId = agreement.getId();
        this.year = (short) enrichedIncomingDataRecord.getRecordedAt().getYear();
        this.month = (byte) enrichedIncomingDataRecord.getRecordedAt().getMonth().getValue();
        this.recordedAt = Date.from(enrichedIncomingDataRecord.getRecordedAt().toInstant());
        this.internalRecordId = UUID.fromString(enrichedIncomingDataRecord.getInternalId());
        this.recordedBytes = enrichedIncomingDataRecord.getRecordedBytes();
        this.maxBytesInBillingPeriod = agreement.getMaxBytesInBillingPeriod();
    }
}
