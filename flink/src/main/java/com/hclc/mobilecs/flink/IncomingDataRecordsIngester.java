package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.cassandra.CassandraSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;

import java.util.Properties;

public class IncomingDataRecordsIngester {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "ingester");

        FlinkKafkaConsumer<ObjectNode> agreementsKafkaConsumer = new FlinkKafkaConsumer<>(
                "agreements",
                new JSONKeyValueDeserializationSchema(true),
                properties);
        KeyedStream<Agreement, String> agreementsStream = env.addSource(agreementsKafkaConsumer)
                .map(new MapFunction<ObjectNode, Agreement>() {
                    @Override
                    public Agreement map(ObjectNode value) throws Exception {
                        return Agreement.fromJson(value);
                    }
                }).keyBy(new KeySelector<Agreement, String>() {
                    @Override
                    public String getKey(Agreement value) throws Exception {
                        return value.getMsisdn();
                    }
                });

        FlinkKafkaConsumer<ObjectNode> incomingDataRecordsKafkaConsumer = new FlinkKafkaConsumer<>(
                "incoming-data-records",
                new JSONKeyValueDeserializationSchema(true),
                properties);
        KeyedStream<EnrichedIncomingDataRecord, String> incomingDataRecordsStream = env.addSource(incomingDataRecordsKafkaConsumer)
                .map(new MapFunction<ObjectNode, EnrichedIncomingDataRecord>() {
                    @Override
                    public EnrichedIncomingDataRecord map(ObjectNode value) throws Exception {
                        return EnrichedIncomingDataRecord.fromJson(value);
                    }
                }).keyBy(new KeySelector<EnrichedIncomingDataRecord, String>() {
                    @Override
                    public String getKey(EnrichedIncomingDataRecord value) throws Exception {
                        return value.getMsisdn();
                    }
                });

        SingleOutputStreamOperator<DataRecord> dataRecordsStream = agreementsStream.connect(incomingDataRecordsStream).flatMap(new MatchingFunction());

        CassandraSink.addSink(dataRecordsStream)
                .setHost("127.0.0.1")
                .build();

        env.execute();
    }

}
