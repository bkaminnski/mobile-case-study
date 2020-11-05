package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.cassandra.CassandraSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;

import java.util.Properties;

import static org.apache.flink.streaming.api.TimeCharacteristic.EventTime;

public class IncomingDataRecordsIngester {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = createExecutionEnvironment();

        KeyedStream<DataRecord, String> dataRecordsStream = agreementsStream(env)
                .connect(incomingDataRecordsStream(env))
                .flatMap(new MatchingFunction())
                .keyBy(IncomingDataRecordsIngester::agreementIdInDataRecord);

        CassandraSink.addSink(dataRecordsStream)
                .setHost("127.0.0.1")
                .build();

        SingleOutputStreamOperator<DataRecordAggregate> aggregatedDataRecords = dataRecordsStream
                .window(new BillingPeriodWindowAssigner())
                .trigger(new DataPlanTrigger())
                .aggregate(new LatestDataRecordAggregatingRecordedBytes(), new DataUsageWindowFunction());

        aggregatedDataRecords.print();

        env.execute();
    }

    private static StreamExecutionEnvironment createExecutionEnvironment() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(EventTime);
        return env;
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
        agreementsKafkaConsumer.setStartFromEarliest();
        agreementsKafkaConsumer.assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps());
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
        incomingDataRecordsKafkaConsumer.assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps());
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

    private static String agreementIdInDataRecord(DataRecord value) throws Exception {
        return value.getAgreementId().toString();
    }
}
