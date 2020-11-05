package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.functions.AggregateFunction;

public class LatestDataRecordAggregatingRecordedBytes implements AggregateFunction<DataRecord, DataRecord, DataRecord> {
    @Override
    public DataRecord createAccumulator() {
        DataRecord dataRecord = new DataRecord();
        dataRecord.setRecordedBytes(0L);
        return dataRecord;
    }

    @Override
    public DataRecord add(DataRecord value, DataRecord accumulator) {
        return value.plusRecordedBytes(accumulator);
    }

    @Override
    public DataRecord getResult(DataRecord accumulator) {
        return accumulator;
    }

    @Override
    public DataRecord merge(DataRecord a, DataRecord b) {
        return a.laterPlusRecordedBytes(b);
    }
}
