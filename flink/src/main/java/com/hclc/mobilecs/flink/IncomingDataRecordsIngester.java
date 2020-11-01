package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.util.Collector;

import java.util.*;

public class IncomingDataRecordsIngester {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "ingester");

        FlinkKafkaConsumer<ObjectNode> dataRecordsKafkaConsumer = new FlinkKafkaConsumer<>(
                "incoming-data-records",
                new JSONKeyValueDeserializationSchema(true),
                properties);
        KeyedStream<EnrichedIncomingDataRecord, String> incomingDataRecordsStream = env.addSource(dataRecordsKafkaConsumer)
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


        env.execute();
    }

}
