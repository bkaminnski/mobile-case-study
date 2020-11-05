package com.hclc.mobilecs.flink;

import java.util.Date;
import java.util.UUID;

class DataRecordAggregate {
    private final UUID agreementId;
    private final short year;
    private final byte month;
    private final Date latestRecordedAt;
    private final UUID latestInternalRecordId;
    private final Long totalRecordedBytes;
    private final String billingPeriodTimeZone;
    private final long maxBytesInBillingPeriod;
    private final AggregateType type;

    DataRecordAggregate(DataRecord dataRecord, AggregateType type) {
        this.agreementId = dataRecord.getAgreementId();
        this.year = dataRecord.getYear();
        this.month = dataRecord.getMonth();
        this.latestRecordedAt = dataRecord.getRecordedAt();
        this.latestInternalRecordId = dataRecord.getInternalRecordId();
        this.totalRecordedBytes = dataRecord.getRecordedBytes();
        this.billingPeriodTimeZone = dataRecord.getBillingPeriodTimeZone();
        this.maxBytesInBillingPeriod = dataRecord.getMaxBytesInBillingPeriod();
        this.type = type;
    }

    enum AggregateType {
        DATA_PLAN_EXCEEDED, BILLING_PERIOD_CLOSED
    }

    @Override
    public String toString() {
        return "DataRecordAggregate{" +
                "type=" + type +
                ", agreementId=" + agreementId +
                ", latestRecordedAt=" + latestRecordedAt +
                ", latestInternalRecordId=" + latestInternalRecordId +
                ", totalRecordedBytes=" + totalRecordedBytes +
                ", maxBytesInBillingPeriod=" + maxBytesInBillingPeriod +
                '}';
    }
}
