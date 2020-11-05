package com.hclc.mobilecs.flink;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.ZonedDateTime;

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

    EnrichedIncomingDataRecord(IncomingDataRecord incomingDataRecord, String internalId) {
        this.incomingDataRecord = incomingDataRecord;
        this.internalId = internalId;
    }

    long getEventTimestampMillis() {
        return incomingDataRecord.getRecordedAt().toInstant().toEpochMilli();
    }

    ZonedDateTime getRecordedAt() {
        return incomingDataRecord.getRecordedAt();
    }

    String getMsisdn() {
        return incomingDataRecord.getMsisdn();
    }

    long getRecordedBytes() {
        return incomingDataRecord.getRecordedBytes();
    }

    String getInternalId() {
        return internalId;
    }

    int getYear() {
        return incomingDataRecord.getRecordedAt().minusSeconds(1).getYear();
    }

    int getMonth() {
        return incomingDataRecord.getRecordedAt().minusSeconds(1).getMonth().getValue();
    }

    static EnrichedIncomingDataRecord fromJson(ObjectNode objectNode) {
        JsonNode value = objectNode.get("value");
        String externalId = value.get("externalId").asText();
        ZonedDateTime recordedAt = ZonedDateTime.parse(value.get("recordedAt").asText());
        String msisdn = value.get("msisdn").asText();
        long recordedBytes = value.get("recordedBytes").asLong();
        IncomingDataRecord incomingDataRecord = new IncomingDataRecord(externalId, recordedAt, msisdn, recordedBytes);
        String internalId = value.get("internalId").asText();
        return new EnrichedIncomingDataRecord(incomingDataRecord, internalId);
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
