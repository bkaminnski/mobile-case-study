package com.hclc.mobilecs.flink;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.ZonedDateTime;

class IncomingDataRecord {
    private final String externalId;
    private final ZonedDateTime recordedAt;
    private final String msisdn;
    private final long recordedBytes;

    IncomingDataRecord(String externalId, ZonedDateTime recordedAt, String msisdn, long recordedBytes) {
        this.externalId = externalId;
        this.recordedAt = recordedAt;
        this.msisdn = msisdn;
        this.recordedBytes = recordedBytes;
    }

    String getExternalId() {
        return externalId;
    }

    ZonedDateTime getRecordedAt() {
        return recordedAt;
    }

    String getMsisdn() {
        return msisdn;
    }

    long getRecordedBytes() {
        return recordedBytes;
    }

    ObjectNode toObjectNode(ObjectMapper objectMapper) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("externalId", externalId);
        objectNode.put("recordedAt", recordedAt.toString());
        objectNode.put("msisdn", msisdn);
        objectNode.put("recordedBytes", recordedBytes);
        return objectNode;
    }
}
