package com.hclc.mobilecs.backend.incomingdatarecords;

import java.time.ZonedDateTime;

class IncomingDataRecord {
    private final String id;
    private final ZonedDateTime recordedAt;
    private final String msisdn;
    private final long recordedBytes;

    IncomingDataRecord(String id, ZonedDateTime recordedAt, String msisdn, long recordedBytes) {
        this.id = id;
        this.recordedAt = recordedAt;
        this.msisdn = msisdn;
        this.recordedBytes = recordedBytes;
    }

    String getMsisdn() {
        return msisdn;
    }

    ZonedDateTime getRecordedAt() {
        return recordedAt;
    }

    String toCsv() {
        return id + ";" + recordedAt + ";" + msisdn + ";" + recordedBytes;
    }
}
