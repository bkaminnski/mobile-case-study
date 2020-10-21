package com.hclc.mobile.backend.datarecords;

import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.data.cassandra.core.cql.Ordering.DESCENDING;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;

@PrimaryKeyClass
class DataRecordKey implements Serializable {
    @PrimaryKeyColumn(ordinal = 0, type = PARTITIONED)
    private final UUID agreementId;
    @PrimaryKeyColumn(ordinal = 1, type = PARTITIONED)
    private final short year;
    @PrimaryKeyColumn(ordinal = 2, type = PARTITIONED)
    private final byte month;
    @PrimaryKeyColumn(ordinal = 3, type = CLUSTERED, ordering = DESCENDING)
    private final Date recordedAt;
    @PrimaryKeyColumn(ordinal = 4, type = CLUSTERED)
    private final UUID internalRecordId;

    DataRecordKey(UUID agreementId, short year, byte month, Date recordedAt, UUID internalRecordId) {
        this.agreementId = agreementId;
        this.year = year;
        this.month = month;
        this.recordedAt = recordedAt;
        this.internalRecordId = internalRecordId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataRecordKey that = (DataRecordKey) o;
        return year == that.year &&
                month == that.month &&
                Objects.equals(agreementId, that.agreementId) &&
                Objects.equals(recordedAt, that.recordedAt) &&
                Objects.equals(internalRecordId, that.internalRecordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agreementId, year, month, recordedAt, internalRecordId);
    }

    @Override
    public String toString() {
        return "DataRecordKey{" +
                "agreementId=" + agreementId +
                ", year=" + year +
                ", month=" + month +
                ", recordedAt=" + recordedAt +
                ", internalRecordId=" + internalRecordId +
                '}';
    }
}
