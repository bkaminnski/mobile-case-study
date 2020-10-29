package com.hclc.mobilecs.flink;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.UUID;

@Table(keyspace = "mobilecs", name = "data_record")
class DataRecord {
    @Column(name = "agreement_id")
    private UUID agreementId;
    @Column(name = "year")
    private short year;
    @Column(name = "month")
    private byte month;
    @Column(name = "recorded_at")
    private Date recordedAt;
    @Column(name = "internal_record_id")
    private UUID internalRecordId;
    @Column(name = "recorded_bytes")
    private Long recordedBytes;

    DataRecord() {
        this.agreementId = null;
        this.year = 0;
        this.month = 0;
        this.recordedAt = null;
        this.internalRecordId = null;
        this.recordedBytes = null;
    }

    DataRecord(UUID agreementId, EnrichedIncomingDataRecord enrichedIncomingDataRecord) {
        this.agreementId = agreementId;
        this.year = (short) enrichedIncomingDataRecord.getRecordedAt().getYear();
        this.month = (byte) enrichedIncomingDataRecord.getRecordedAt().getMonth().getValue();
        this.recordedAt = Date.from(enrichedIncomingDataRecord.getRecordedAt().toInstant());
        this.internalRecordId = UUID.fromString(enrichedIncomingDataRecord.getInternalId());
        this.recordedBytes = enrichedIncomingDataRecord.getRecordedBytes();
    }

    public UUID getAgreementId() {
        return agreementId;
    }

    public short getYear() {
        return year;
    }

    public byte getMonth() {
        return month;
    }

    public Date getRecordedAt() {
        return recordedAt;
    }

    public UUID getInternalRecordId() {
        return internalRecordId;
    }

    public Long getRecordedBytes() {
        return recordedBytes;
    }

    public void setAgreementId(UUID agreementId) {
        this.agreementId = agreementId;
    }

    public void setYear(short year) {
        this.year = year;
    }

    public void setMonth(byte month) {
        this.month = month;
    }

    public void setRecordedAt(Date recordedAt) {
        this.recordedAt = recordedAt;
    }

    public void setInternalRecordId(UUID internalRecordId) {
        this.internalRecordId = internalRecordId;
    }

    public void setRecordedBytes(Long recordedBytes) {
        this.recordedBytes = recordedBytes;
    }
}
