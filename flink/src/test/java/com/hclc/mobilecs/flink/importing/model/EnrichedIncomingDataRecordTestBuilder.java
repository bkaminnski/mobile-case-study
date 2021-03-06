package com.hclc.mobilecs.flink.importing.model;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.ZonedDateTime;
import java.util.UUID;

import static com.hclc.mobilecs.flink.Fixtures.*;

public class EnrichedIncomingDataRecordTestBuilder {
    private UUID externalId = IRRELEVANT_UUID;
    private ZonedDateTime recordedAt = IRRELEVANT_TIMESTAMP;
    private String msisdn = IRRELEVANT_STRING;
    private long recordedBytes = IRRELEVANT_LONG;
    private UUID internalId = IRRELEVANT_UUID;

    private EnrichedIncomingDataRecordTestBuilder() {
    }

    public static EnrichedIncomingDataRecordTestBuilder anEnrichedIncomingDataRecord() {
        return new EnrichedIncomingDataRecordTestBuilder();
    }

    public EnrichedIncomingDataRecordTestBuilder recordedAt(ZonedDateTime recordedAt) {
        this.recordedAt = recordedAt;
        return this;
    }

    public EnrichedIncomingDataRecord build() {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("externalId", externalId.toString());
        objectNode.put("recordedAt", recordedAt.toString());
        objectNode.put("msisdn", msisdn);
        objectNode.put("recordedBytes", recordedBytes);
        objectNode.put("internalId", internalId.toString());
        ObjectNode wrappingNode = objectMapper.createObjectNode();
        wrappingNode.set("value", objectNode);
        return EnrichedIncomingDataRecord.fromJson(wrappingNode);
    }
}
