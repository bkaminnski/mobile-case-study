package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Properties;

import static java.time.ZonedDateTime.parse;

public class HighUsageFiltering {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<IncomingDataRecord> incomingDataRecords = env.fromElements(
                new IncomingDataRecord("1", parse("2020-01-01T01:00:00+01:00[Europe/Warsaw]"), "4841342423", 8011),
                new IncomingDataRecord("2", parse("2020-01-01T02:00:00+01:00[Europe/Warsaw]"), "4841342423", 7002112),
                new IncomingDataRecord("3", parse("2020-01-01T03:00:00+01:00[Europe/Warsaw]"), "4841342423", 90112),
                new IncomingDataRecord("4", parse("2020-01-01T04:00:00+01:00[Europe/Warsaw]"), "4841342423", 2002112)
        );

        DataStream<String> highUsage = incomingDataRecords.filter(new FilterFunction<IncomingDataRecord>() {
            @Override
            public boolean filter(IncomingDataRecord incomingDataRecord) throws Exception {
                return incomingDataRecord.getRecordedBytes() >= 1024 * 1024;
            }
        }).map(new MapFunction<IncomingDataRecord, String>() {
            @Override
            public String map(IncomingDataRecord incomingDataRecord) throws Exception {
                return incomingDataRecord.toString();
            }
        });

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        FlinkKafkaProducer<String> kafkaProducer = new FlinkKafkaProducer<>(
                "incoming-data-records-high-usage",
                new SimpleStringSchema(),
                properties);
        highUsage.addSink(kafkaProducer);

        highUsage.print();

        env.execute();
    }
}
