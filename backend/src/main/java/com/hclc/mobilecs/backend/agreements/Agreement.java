package com.hclc.mobilecs.backend.agreements;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.ZonedDateTime;
import java.util.UUID;

class Agreement {
    private final UUID id;
    private final String msisdn;
    private final ZonedDateTime signedAt;
    private final ZonedDateTime serviceStartAt;
    private final long maxBytesInBillingPeriod;

    Agreement(UUID id, String msisdn, ZonedDateTime signedAt, ZonedDateTime serviceStartAt, long maxBytesInBillingPeriod) {
        this.id = id;
        this.msisdn = msisdn;
        this.signedAt = signedAt;
        this.serviceStartAt = serviceStartAt;
        this.maxBytesInBillingPeriod = maxBytesInBillingPeriod;
    }

    UUID getId() {
        return id;
    }

    String getMsisdn() {
        return msisdn;
    }

    ZonedDateTime getSignedAt() {
        return signedAt;
    }

    String toJson(ObjectMapper objectMapper) {
        try {
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("id", id.toString());
            objectNode.put("msisdn", msisdn);
            objectNode.put("signedAt", signedAt.toString());
            objectNode.put("serviceStartAt", serviceStartAt.toString());
            objectNode.put("maxBytesInBillingPeriod", maxBytesInBillingPeriod);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
