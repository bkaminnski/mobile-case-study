package com.hclc.mobilecs.flink;

import com.datastax.driver.mapping.Mapper;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.cassandra.CassandraSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;

import java.util.Properties;
import java.util.UUID;

public class IncomingDataRecordsIngester {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "ingester");

        FlinkKafkaConsumer<ObjectNode> kafkaConsumer = new FlinkKafkaConsumer<>(
                "incoming-data-records",
                new JSONKeyValueDeserializationSchema(true),
                properties);

        SingleOutputStreamOperator<DataRecord> stream = env.addSource(kafkaConsumer)
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
                }).map(new MapFunction<EnrichedIncomingDataRecord, DataRecord>() {
                    @Override
                    public DataRecord map(EnrichedIncomingDataRecord value) throws Exception {
                        return new DataRecord(UUID.randomUUID(), value);
                    }
                });

        CassandraSink.addSink(stream)
                .setHost("127.0.0.1")
                .build();

        stream.print();

        env.execute();
    }
}
