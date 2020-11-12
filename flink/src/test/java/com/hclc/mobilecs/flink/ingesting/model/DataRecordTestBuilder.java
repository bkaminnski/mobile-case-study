package com.hclc.mobilecs.flink.ingesting.model;

import java.time.ZonedDateTime;

import static com.hclc.mobilecs.flink.Fixtures.IRRELEVANT_STRING;
import static com.hclc.mobilecs.flink.Fixtures.IRRELEVANT_TIMESTAMP;
import static com.hclc.mobilecs.flink.importing.model.EnrichedIncomingDataRecordTestBuilder.anEnrichedIncomingDataRecord;
import static com.hclc.mobilecs.flink.ingesting.model.AgreementTestBuilder.anAgreement;

public class DataRecordTestBuilder {
    private String billingPeriodTimeZone = IRRELEVANT_STRING;
    private ZonedDateTime recordedAt = IRRELEVANT_TIMESTAMP;

    private DataRecordTestBuilder() {
    }

    public static DataRecordTestBuilder aDataRecord() {
        return new DataRecordTestBuilder();
    }

    public DataRecordTestBuilder withBillingPeriodTimeZone(String billingPeriodTimeZone) {
        this.billingPeriodTimeZone = billingPeriodTimeZone;
        return this;
    }

    public DataRecordTestBuilder recordedAt(ZonedDateTime recordedAt) {
        this.recordedAt = recordedAt;
        return this;
    }

    public DataRecord build() {
        return new DataRecord(
                anAgreement().withBillingPeriodTimeZone(billingPeriodTimeZone).build(),
                anEnrichedIncomingDataRecord().recordedAt(recordedAt).build()
        );
    }
}
