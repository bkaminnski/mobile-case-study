package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.operators.co.CoStreamFlatMap;
import org.apache.flink.streaming.util.KeyedTwoInputStreamOperatorTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingFunctionTest {
    private static final UUID AGREEMENT1_ID = UUID.fromString("bcda9b4a-7f0a-4901-8fe3-24729b3bf37b");
    private static final String AGREEMENT1_MSISDN = "48601443123";
    private static final ZonedDateTime AGREEMENT1_SIGNED_AT = ZonedDateTime.parse("2019-12-22T17:33:53+01:00[Europe/Warsaw]");
    private static final ZonedDateTime AGREEMENT1_SERVICE_START_AT = ZonedDateTime.parse("2020-01-01T00:00:00+01:00[Europe/Warsaw]");
    private static final long AGREEMENT1_MAX_BYTES = 10L * 1024 * 1024 * 1024; // 10 GB
    private static final UUID INCOMING_DATA_RECORD1_EXTERNAL_ID = UUID.fromString("fd9a3848-560c-4f23-b510-6cb1d497a19d");
    private static final UUID INCOMING_DATA_RECORD1_INTERNAL_ID = UUID.fromString("1f77f79e-25a0-4927-9fad-e0e8fad1c95b");
    private static final long INCOMING_DATA_RECORD1_RECORDED_BYTES = 44321245;
    private static final ZonedDateTime INCOMING_DATA_RECORD1_GENERATED_AT = ZonedDateTime.parse("2020-01-01T01:00:00+01:00[Europe/Warsaw]");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MatchingFunction matchingFunction;
    private KeyedTwoInputStreamOperatorTestHarness<String, Agreement, EnrichedIncomingDataRecord, DataRecord> testHarness;

    @BeforeEach
    void beforeEach() throws Exception {
        matchingFunction = new MatchingFunction();
        CoStreamFlatMap<Agreement, EnrichedIncomingDataRecord, DataRecord> coStreamFlatMapOperator = new CoStreamFlatMap<>(matchingFunction);
        testHarness = new KeyedTwoInputStreamOperatorTestHarness<>(coStreamFlatMapOperator, Agreement::getMsisdn, EnrichedIncomingDataRecord::getMsisdn, Types.STRING);
        testHarness.open();
    }

    @Test
    void whenAgreementAndNoIncomingDataRecord_shouldStoreAgreementAndEmitNothing() throws Exception {
        Agreement agreement = arrangeAgreement1();

        testHarness.processElement1(agreement, AGREEMENT1_SIGNED_AT.toInstant().toEpochMilli());

        assertThat(storedIncomingDataRecords()).isEmpty();
        assertThat(storedAgreements()).hasSize(1).first().usingRecursiveComparison().isEqualTo(agreement);
        assertThat(output()).isEmpty();
    }

    @Test
    void whenIncomingDataRecordAndNoAgreement_shouldStoreIncomingDataRecordAndEmitNothing() throws Exception {
        EnrichedIncomingDataRecord incomingDataRecord = arrangeIncomingDataRecord1();

        testHarness.processElement2(incomingDataRecord, INCOMING_DATA_RECORD1_GENERATED_AT.toInstant().toEpochMilli());

        assertThat(storedAgreements()).isEmpty();
        assertThat(storedIncomingDataRecords()).hasSize(1).first().usingRecursiveComparison().isEqualTo(incomingDataRecord);
        assertThat(output()).isEmpty();
    }

    @Test
    void whenAgreementAndIncomingDataRecord_shouldStoreAgreementAndEmitDataRecord() throws Exception {
        Agreement agreement = arrangeAgreement1();
        EnrichedIncomingDataRecord incomingDataRecord = arrangeIncomingDataRecord1();

        testHarness.processElement1(agreement, AGREEMENT1_SIGNED_AT.toInstant().toEpochMilli());
        testHarness.processElement2(incomingDataRecord, INCOMING_DATA_RECORD1_GENERATED_AT.toInstant().toEpochMilli());

        assertThat(storedIncomingDataRecords()).isEmpty();
        assertThat(storedAgreements()).hasSize(1).first().usingRecursiveComparison().isEqualTo(agreement);
        assertThat(output()).hasSize(1).first().usingRecursiveComparison().isEqualTo(expectedDataRecord(agreement, incomingDataRecord));
    }

    private Agreement arrangeAgreement1() {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("id", AGREEMENT1_ID.toString());
        objectNode.put("msisdn", AGREEMENT1_MSISDN);
        objectNode.put("signedAt", AGREEMENT1_SIGNED_AT.toString());
        objectNode.put("serviceStartAt", AGREEMENT1_SERVICE_START_AT.toString());
        objectNode.put("maxBytesInBillingPeriod", AGREEMENT1_MAX_BYTES);
        ObjectNode wrappingNode = objectMapper.createObjectNode();
        wrappingNode.set("value", objectNode);
        return Agreement.fromJson(wrappingNode);
    }

    private Iterable<Agreement> storedAgreements() throws Exception {
        return matchingFunction.getRuntimeContext().getListState(new ListStateDescriptor<>("agreements", Agreement.class)).get();
    }

    private EnrichedIncomingDataRecord arrangeIncomingDataRecord1() {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("externalId", INCOMING_DATA_RECORD1_EXTERNAL_ID.toString());
        objectNode.put("recordedAt", INCOMING_DATA_RECORD1_GENERATED_AT.toString());
        objectNode.put("msisdn", AGREEMENT1_MSISDN);
        objectNode.put("recordedBytes", INCOMING_DATA_RECORD1_RECORDED_BYTES);
        objectNode.put("internalId", INCOMING_DATA_RECORD1_INTERNAL_ID.toString());
        ObjectNode wrappingNode = objectMapper.createObjectNode();
        wrappingNode.set("value", objectNode);
        return EnrichedIncomingDataRecord.fromJson(wrappingNode);
    }

    private Iterable<EnrichedIncomingDataRecord> storedIncomingDataRecords() throws Exception {
        return matchingFunction.getRuntimeContext().getListState(new ListStateDescriptor<>("incomingDataRecords", EnrichedIncomingDataRecord.class)).get();
    }

    private DataRecord expectedDataRecord(Agreement agreement, EnrichedIncomingDataRecord incomingDataRecord) {
        return new DataRecord(agreement, incomingDataRecord);
    }

    private List<DataRecord> output() {
        return testHarness.extractOutputValues();
    }
}