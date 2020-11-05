package com.hclc.mobilecs.flink;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.ZonedDateTime;
import java.util.UUID;

class Agreement implements Comparable<Agreement> {
    private final UUID id;
    private final String msisdn;
    private final ZonedDateTime signedAt;
    private final ZonedDateTime serviceStartAt;
    private final String billingPeriodTimeZone;
    private final long maxBytesInBillingPeriod;

    private Agreement(UUID id, String msisdn, ZonedDateTime signedAt, ZonedDateTime serviceStartAt, String billingPeriodTimeZone, long maxBytesInBillingPeriod) {
        this.id = id;
        this.msisdn = msisdn;
        this.signedAt = signedAt;
        this.serviceStartAt = serviceStartAt;
        this.billingPeriodTimeZone = billingPeriodTimeZone;
        this.maxBytesInBillingPeriod = maxBytesInBillingPeriod;
    }

    static Agreement fromJson(ObjectNode objectNode) {
        JsonNode value = objectNode.get("value");
        return new Agreement(
                UUID.fromString(value.get("id").asText()),
                value.get("msisdn").asText(),
                ZonedDateTime.parse(value.get("signedAt").asText()),
                ZonedDateTime.parse(value.get("serviceStartAt").asText()),
                value.get("billingPeriodTimeZone").asText(),
                value.get("maxBytesInBillingPeriod").asLong()
        );
    }

    UUID getId() {
        return id;
    }

    String getMsisdn() {
        return msisdn;
    }

    ZonedDateTime getServiceStartAt() {
        return serviceStartAt;
    }

    String getBillingPeriodTimeZone() {
        return billingPeriodTimeZone;
    }

    long getMaxBytesInBillingPeriod() {
        return maxBytesInBillingPeriod;
    }

    @Override
    public int compareTo(Agreement o) {
        if (this.serviceStartAt.compareTo(o.serviceStartAt) == 0) {
            return -1 * this.signedAt.compareTo(o.signedAt);
        } else {
            return -1 * this.serviceStartAt.compareTo(o.serviceStartAt);
        }
    }
}
