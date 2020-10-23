package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import static java.time.Instant.parse;
import static java.util.Date.from;

public class Application {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<IncomingDataRecord> incomingDataRecords = env.fromElements(
                new IncomingDataRecord(from(parse("2020-01-01T01:00:00+01:00[Europe/Warsaw]")), "4841342423", 8011),
                new IncomingDataRecord(from(parse("2020-01-01T02:00:00+01:00[Europe/Warsaw]")), "4841342423", 7002112),
                new IncomingDataRecord(from(parse("2020-01-01T03:00:00+01:00[Europe/Warsaw]")), "4841342423", 90112),
                new IncomingDataRecord(from(parse("2020-01-01T04:00:00+01:00[Europe/Warsaw]")), "4841342423", 2002112)
        );

        DataStream<IncomingDataRecord> highUsage = incomingDataRecords.filter(new FilterFunction<IncomingDataRecord>() {
            @Override
            public boolean filter(IncomingDataRecord incomingDataRecord) throws Exception {
                return incomingDataRecord.getRecordedBytes() >= 1024 * 1024;
            }
        });

        highUsage.print();

        env.execute();
    }
}
