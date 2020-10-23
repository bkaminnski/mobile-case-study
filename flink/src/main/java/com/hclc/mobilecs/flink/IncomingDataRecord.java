package com.hclc.mobilecs.flink;

import java.util.Date;

public class IncomingDataRecord {
    private final Date recordedAt;
    private final String msisdn;
    private final long recordedBytes;

    public IncomingDataRecord(Date recordedAt, String msisdn, long recordedBytes) {
        this.recordedAt = recordedAt;
        this.msisdn = msisdn;
        this.recordedBytes = recordedBytes;
    }

    public long getRecordedBytes() {
        return recordedBytes;
    }

    @Override
    public String toString() {
        return "IncomingDataRecord{" +
                "recordedAt=" + recordedAt +
                ", msisdn='" + msisdn + '\'' +
                ", recordedBytes=" + recordedBytes +
                '}';
    }
}
