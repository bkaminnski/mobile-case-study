package com.hclc.mobilecs.flink;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
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

        SingleOutputStreamOperator<DataRecord> dataRecordsStream = agreementsStream(env)
                .connect(incomingDataRecordsStream(env))
                .flatMap(new MatchingFunction());

        CassandraSink.addSink(dataRecordsStream)
                .setHost("127.0.0.1")
                .build();

        env.execute();
    }

    private static KeyedStream<Agreement, String> agreementsStream(StreamExecutionEnvironment env) {
        return readingAgreementsFromKafka(env)
                .map(IncomingDataRecordsIngester::agreementFromJson)
                .keyBy(IncomingDataRecordsIngester::msisdnInAgreement);
    }

    private static DataStreamSource<ObjectNode> readingAgreementsFromKafka(StreamExecutionEnvironment env) {
        FlinkKafkaConsumer<ObjectNode> agreementsKafkaConsumer = new FlinkKafkaConsumer<>(
                "agreements",
                new JSONKeyValueDeserializationSchema(true),
                kafkaProperties()
        );
        return env.addSource(agreementsKafkaConsumer);
    }

    private static Agreement agreementFromJson(ObjectNode value) throws Exception {
        return Agreement.fromJson(value);
    }

    private static String msisdnInAgreement(Agreement value) throws Exception {
        return value.getMsisdn();
    }

    private static KeyedStream<EnrichedIncomingDataRecord, String> incomingDataRecordsStream(StreamExecutionEnvironment env) {
        return readingIncomingDataRecordsFromKafka(env)
                .map(IncomingDataRecordsIngester::incomingDataRecordFromJson)
                .keyBy(IncomingDataRecordsIngester::msisdnInIncomingDataRecord);
    }

    private static DataStreamSource<ObjectNode> readingIncomingDataRecordsFromKafka(StreamExecutionEnvironment env) {
        FlinkKafkaConsumer<ObjectNode> incomingDataRecordsKafkaConsumer = new FlinkKafkaConsumer<>(
                "incoming-data-records",
                new JSONKeyValueDeserializationSchema(true),
                kafkaProperties());
        return env.addSource(incomingDataRecordsKafkaConsumer);
    }

    private static EnrichedIncomingDataRecord incomingDataRecordFromJson(ObjectNode value) throws Exception {
        return EnrichedIncomingDataRecord.fromJson(value);
    }

    private static String msisdnInIncomingDataRecord(EnrichedIncomingDataRecord value) throws Exception {
        return value.getMsisdn();
    }

    private static Properties kafkaProperties() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "ingester");
        return properties;
    }
}
