package com.hclc.mobilecs.flink;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

class EnrichedIncomingDataRecord {
    private final IncomingDataRecord incomingDataRecord;
    private final String internalId;

    EnrichedIncomingDataRecord(IncomingDataRecord incomingDataRecord) {
        this.incomingDataRecord = incomingDataRecord;
        // In production system, external id could be anything, not necessarily globally unique.
        // To guarantee global uniqueness, each imported record would receive a new UUID.
        // This however would not be deterministic. Knowing that in the case study incoming data record identifier
        // is already a UUID, here it is simply reused as an internal record identifier to produce consistent results
        // while playing with the case study.
        this.internalId = incomingDataRecord.getExternalId();
    }

    long getEventTimestampMillis() {
        return incomingDataRecord.getRecordedAt().toInstant().toEpochMilli();
    }

    String getMsisdn() {
        return incomingDataRecord.getMsisdn();
    }

    String toJson(ObjectMapper objectMapper) {
        try {
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.setAll(incomingDataRecord.toObjectNode(objectMapper));
            objectNode.put("internalId", internalId);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
