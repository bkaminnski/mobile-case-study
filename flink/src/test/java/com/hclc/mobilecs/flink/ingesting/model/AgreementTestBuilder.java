package com.hclc.mobilecs.flink.ingesting.model;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.ZonedDateTime;
import java.util.UUID;

import static com.hclc.mobilecs.flink.Fixtures.*;

public class AgreementTestBuilder {
    private UUID id = IRRELEVANT_UUID;
    private String msisdn = IRRELEVANT_STRING;
    private ZonedDateTime signedAt = IRRELEVANT_TIMESTAMP;
    private ZonedDateTime serviceStartAt = IRRELEVANT_TIMESTAMP;
    private String billingPeriodTimeZone = IRRELEVANT_STRING;
    private long maxBytesInBillingPeriod = IRRELEVANT_LONG;

    private AgreementTestBuilder() {
    }

    public static AgreementTestBuilder anAgreement() {
        return new AgreementTestBuilder();
    }

    public AgreementTestBuilder signedAt(ZonedDateTime signedAt) {
        this.signedAt = signedAt;
        return this;
    }

    public AgreementTestBuilder withServiceStartAt(ZonedDateTime serviceStartAt) {
        this.serviceStartAt = serviceStartAt;
        return this;
    }

    AgreementTestBuilder withBillingPeriodTimeZone(String billingPeriodTimeZone) {
        this.billingPeriodTimeZone = billingPeriodTimeZone;
        return this;
    }

    public Agreement build() {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("id", id.toString());
        objectNode.put("msisdn", msisdn);
        objectNode.put("signedAt", signedAt.toString());
        objectNode.put("serviceStartAt", serviceStartAt.toString());
        objectNode.put("billingPeriodTimeZone", billingPeriodTimeZone);
        objectNode.put("maxBytesInBillingPeriod", maxBytesInBillingPeriod);
        ObjectNode wrappingNode = objectMapper.createObjectNode();
        wrappingNode.set("value", objectNode);
        return Agreement.fromJson(wrappingNode);
    }
}
