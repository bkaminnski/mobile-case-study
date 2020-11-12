package com.hclc.mobilecs.flink.ingesting.model;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.hclc.mobilecs.flink.importing.model.EnrichedIncomingDataRecord;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Table(keyspace = "mobilecs", name = "data_record")
public class DataRecord implements Serializable {
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
    @Transient
    private String billingPeriodTimeZone;
    @Transient
    private long maxBytesInBillingPeriod;

    public DataRecord() {
    }

    public DataRecord(Agreement agreement, EnrichedIncomingDataRecord enrichedIncomingDataRecord) {
        this.agreementId = agreement.getId();
        this.year = (short) enrichedIncomingDataRecord.getYear();
        this.month = (byte) enrichedIncomingDataRecord.getMonth();
        this.recordedAt = Date.from(enrichedIncomingDataRecord.getRecordedAt().toInstant());
        this.internalRecordId = UUID.fromString(enrichedIncomingDataRecord.getInternalId());
        this.recordedBytes = enrichedIncomingDataRecord.getRecordedBytes();
        this.billingPeriodTimeZone = agreement.getBillingPeriodTimeZone();
        this.maxBytesInBillingPeriod = agreement.getMaxBytesInBillingPeriod();
    }

    public DataRecord plusRecordedBytes(DataRecord other) {
        DataRecord dataRecord = new DataRecord();
        dataRecord.setAgreementId(agreementId);
        dataRecord.setYear(year);
        dataRecord.setMonth(month);
        dataRecord.setRecordedAt(recordedAt);
        dataRecord.setInternalRecordId(internalRecordId);
        dataRecord.setRecordedBytes(this.recordedBytes + other.recordedBytes);
        dataRecord.setBillingPeriodTimeZone(billingPeriodTimeZone);
        dataRecord.setMaxBytesInBillingPeriod(maxBytesInBillingPeriod);
        return dataRecord;
    }

    public DataRecord laterPlusRecordedBytes(DataRecord other) {
        if (this.recordedAt.after(other.recordedAt)) {
            return this.plusRecordedBytes(other);
        }
        return other.plusRecordedBytes(this);
    }

    public UUID getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(UUID agreementId) {
        this.agreementId = agreementId;
    }

    public short getYear() {
        return year;
    }

    public void setYear(short year) {
        this.year = year;
    }

    public byte getMonth() {
        return month;
    }

    public void setMonth(byte month) {
        this.month = month;
    }

    public Date getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Date recordedAt) {
        this.recordedAt = recordedAt;
    }

    public UUID getInternalRecordId() {
        return internalRecordId;
    }

    public void setInternalRecordId(UUID internalRecordId) {
        this.internalRecordId = internalRecordId;
    }

    public Long getRecordedBytes() {
        return recordedBytes;
    }

    public void setRecordedBytes(Long recordedBytes) {
        this.recordedBytes = recordedBytes;
    }

    public String getBillingPeriodTimeZone() {
        return billingPeriodTimeZone;
    }

    public void setBillingPeriodTimeZone(String billingPeriodTimeZone) {
        this.billingPeriodTimeZone = billingPeriodTimeZone;
    }

    public long getMaxBytesInBillingPeriod() {
        return maxBytesInBillingPeriod;
    }

    public void setMaxBytesInBillingPeriod(long maxBytesInBillingPeriod) {
        this.maxBytesInBillingPeriod = maxBytesInBillingPeriod;
    }

    public boolean exceedsDataPlan() {
        return recordedBytes >= maxBytesInBillingPeriod;
    }
}
