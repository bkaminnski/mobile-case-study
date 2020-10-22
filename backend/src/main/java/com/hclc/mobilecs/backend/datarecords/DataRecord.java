package com.hclc.mobilecs.backend.datarecords;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
class DataRecord {
    @PrimaryKey
    private final DataRecordKey key;
    private final Long recordedBytes;

    DataRecord(DataRecordKey key, Long recordedBytes) {
        this.key = key;
        this.recordedBytes = recordedBytes;
    }

    public DataRecordKey getKey() {
        return key;
    }

    public Long getRecordedBytes() {
        return recordedBytes;
    }

    @Override
    public String toString() {
        return "DataRecord{" +
                "key=" + key +
                ", recordedBytes=" + recordedBytes +
                '}';
    }
}
