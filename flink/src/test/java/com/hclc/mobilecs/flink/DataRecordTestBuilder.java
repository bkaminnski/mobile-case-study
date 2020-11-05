package com.hclc.mobilecs.flink;

import java.time.ZonedDateTime;

import static com.hclc.mobilecs.flink.AgreementTestBuilder.anAgreement;
import static com.hclc.mobilecs.flink.EnrichedIncomingDataRecordTestBuilder.anEnrichedIncomingDataRecord;
import static com.hclc.mobilecs.flink.Fixtures.IRRELEVANT_STRING;
import static com.hclc.mobilecs.flink.Fixtures.IRRELEVANT_TIMESTAMP;

class DataRecordTestBuilder {
    private String billingPeriodTimeZone = IRRELEVANT_STRING;
    private ZonedDateTime recordedAt = IRRELEVANT_TIMESTAMP;

    private DataRecordTestBuilder() {
    }

    static DataRecordTestBuilder aDataRecord() {
        return new DataRecordTestBuilder();
    }

    DataRecordTestBuilder withBillingPeriodTimeZone(String billingPeriodTimeZone) {
        this.billingPeriodTimeZone = billingPeriodTimeZone;
        return this;
    }

    DataRecordTestBuilder recordedAt(ZonedDateTime recordedAt) {
        this.recordedAt = recordedAt;
        return this;
    }

    DataRecord build() {
        return new DataRecord(
                anAgreement().withBillingPeriodTimeZone(billingPeriodTimeZone).build(),
                anEnrichedIncomingDataRecord().recordedAt(recordedAt).build()
        );
    }
}
