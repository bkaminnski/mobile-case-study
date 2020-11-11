package com.hclc.mobilecs.flink;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

class DataRecordAggregate {
    private static final BigDecimal GIGABYTE = new BigDecimal(1024L * 1024 * 1024);
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

    UUID getAgreementId() {
        return agreementId;
    }

    String toJson(ObjectMapper objectMapper) {
        try {
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("agreementId", agreementId.toString());
            objectNode.put("year", year);
            objectNode.put("month", month);
            objectNode.put("latestRecordedAt", latestRecordedAt.toString());
            objectNode.put("latestInternalRecordId", latestInternalRecordId.toString());
            objectNode.put("totalRecordedBytes", totalRecordedBytes);
            objectNode.put("billingPeriodTimeZone", billingPeriodTimeZone);
            objectNode.put("maxBytesInBillingPeriod", maxBytesInBillingPeriod);
            objectNode.put("type", type.name());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    enum AggregateType {
        DATA_PLAN_EXCEEDED, BILLING_PERIOD_CLOSED
    }
}
