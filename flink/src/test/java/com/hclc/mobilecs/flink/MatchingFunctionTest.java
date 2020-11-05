package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.co.CoStreamFlatMap;
import org.apache.flink.streaming.util.KeyedTwoInputStreamOperatorTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static com.hclc.mobilecs.flink.AgreementTestBuilder.anAgreement;
import static com.hclc.mobilecs.flink.DataRecordTestBuilder.aDataRecord;
import static com.hclc.mobilecs.flink.EnrichedIncomingDataRecordTestBuilder.anEnrichedIncomingDataRecord;
import static org.assertj.core.api.Assertions.assertThat;

class MatchingFunctionTest {
    private static final ZonedDateTime AGREEMENT_SIGNED_AT = ZonedDateTime.parse("2019-12-22T17:33:53+01:00[Europe/Warsaw]");
    private static final ZonedDateTime AGREEMENT_SERVICE_START_AT = ZonedDateTime.parse("2020-01-01T00:00:00+01:00[Europe/Warsaw]");
    private static final ZonedDateTime INCOMING_DATA_RECORD_RECORDED_AT = ZonedDateTime.parse("2020-01-01T07:00:00+01:00[Europe/Warsaw]");
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
        Agreement agreement = arrangeAgreement();

        testHarness.processElement1(agreement, AGREEMENT_SIGNED_AT.toInstant().toEpochMilli());

        assertThat(storedIncomingDataRecords()).isEmpty();
        assertThat(storedAgreements())
                .hasSize(1)
                .first().usingRecursiveComparison().isEqualTo(agreement);
        assertThat(output()).isEmpty();
    }

    @Test
    void whenIncomingDataRecordAndNoAgreement_shouldStoreIncomingDataRecordAndEmitNothing() throws Exception {
        EnrichedIncomingDataRecord incomingDataRecord = arrangeIncomingDataRecord();

        testHarness.processElement2(incomingDataRecord, INCOMING_DATA_RECORD_RECORDED_AT.toInstant().toEpochMilli());

        assertThat(storedAgreements()).isEmpty();
        assertThat(storedIncomingDataRecords())
                .hasSize(1)
                .first().usingRecursiveComparison().isEqualTo(incomingDataRecord);
        assertThat(output()).isEmpty();
    }

    @Test
    void whenAgreementAndIncomingDataRecord_shouldStoreAgreementAndEmitDataRecord() throws Exception {
        Agreement agreement = arrangeAgreement();
        EnrichedIncomingDataRecord incomingDataRecord = arrangeIncomingDataRecord();

        testHarness.processElement1(agreement, AGREEMENT_SIGNED_AT.toInstant().toEpochMilli());
        testHarness.processElement2(incomingDataRecord, INCOMING_DATA_RECORD_RECORDED_AT.toInstant().toEpochMilli());

        assertThat(storedIncomingDataRecords()).isEmpty();
        assertThat(storedAgreements())
                .hasSize(1)
                .first().usingRecursiveComparison().isEqualTo(agreement);
        assertThat(output()).hasSize(1).first().usingRecursiveComparison().isEqualTo(expectedDataRecord());
    }

    private Agreement arrangeAgreement() {
        return anAgreement().signedAt(AGREEMENT_SIGNED_AT).withServiceStartAt(AGREEMENT_SERVICE_START_AT).build();
    }

    private Iterable<Agreement> storedAgreements() throws Exception {
        return matchingFunction.getRuntimeContext().getListState(new ListStateDescriptor<>("agreements", Agreement.class)).get();
    }

    private EnrichedIncomingDataRecord arrangeIncomingDataRecord() {
        return anEnrichedIncomingDataRecord().recordedAt(INCOMING_DATA_RECORD_RECORDED_AT).build();
    }

    private Iterable<EnrichedIncomingDataRecord> storedIncomingDataRecords() throws Exception {
        return matchingFunction.getRuntimeContext().getListState(new ListStateDescriptor<>("incomingDataRecords", EnrichedIncomingDataRecord.class)).get();
    }

    private DataRecord expectedDataRecord() {
        return aDataRecord().recordedAt(INCOMING_DATA_RECORD_RECORDED_AT).build();
    }

    private List<DataRecord> output() {
        return testHarness.extractOutputValues();
    }
}