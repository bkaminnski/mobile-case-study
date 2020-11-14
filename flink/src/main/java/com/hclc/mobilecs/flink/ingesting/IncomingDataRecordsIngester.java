package com.hclc.mobilecs.flink.ingesting;

import com.hclc.mobilecs.flink.importing.model.EnrichedIncomingDataRecord;
import com.hclc.mobilecs.flink.ingesting.model.Agreement;
import com.hclc.mobilecs.flink.ingesting.model.DataRecord;
import com.hclc.mobilecs.flink.ingesting.model.DataRecordAggregate;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.cassandra.CassandraSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

import static com.hclc.mobilecs.flink.Configuration.getCassandraContactPoint;
import static com.hclc.mobilecs.flink.Configuration.getKafkaBootstrapServers;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.of;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.apache.flink.streaming.api.TimeCharacteristic.EventTime;
import static org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer.Semantic.EXACTLY_ONCE;

public class IncomingDataRecordsIngester {
    private static final String INCOMING_DATA_RECORDS_TOPIC = "incoming-data-records";
    private static final String DATA_RECORD_AGGREGATES_TOPIC = "data-records-aggregates";
    private static final String KAFKA_CONSUMER_GROUP = "ingester";
    private static final String AGREEMENTS_TOPIC = "agreements";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = createExecutionEnvironment();

        KeyedStream<DataRecord, String> dataRecordsStream = agreementsStream(env)
                .connect(incomingDataRecordsStream(env))
                .flatMap(new MatchingFunction())
                .keyBy(IncomingDataRecordsIngester::agreementIdInDataRecord);

        CassandraSink.addSink(dataRecordsStream)
                .setHost(getCassandraContactPoint())
                .build();

        dataRecordsStream
                .window(new BillingPeriodWindowAssigner())
                .trigger(new DataPlanTrigger())
                .aggregate(new LatestDataRecordAggregatingRecordedBytes(), new DataUsageWindowFunction())
                .addSink(kafkaProducer())
                .name("Kafka [" + DATA_RECORD_AGGREGATES_TOPIC + "]");

        env.execute("Incoming Data Records Ingester");
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
                AGREEMENTS_TOPIC,
                new JSONKeyValueDeserializationSchema(true),
                kafkaConsumerProperties()
        );
        agreementsKafkaConsumer.setStartFromEarliest();
        WatermarkStrategy<ObjectNode> watermarkStrategy = WatermarkStrategy.<ObjectNode>forMonotonousTimestamps().withIdleness(of(1, SECONDS));
        agreementsKafkaConsumer.assignTimestampsAndWatermarks(watermarkStrategy);
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
                INCOMING_DATA_RECORDS_TOPIC,
                new JSONKeyValueDeserializationSchema(true),
                kafkaConsumerProperties());
        incomingDataRecordsKafkaConsumer.assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps());
        return env.addSource(incomingDataRecordsKafkaConsumer);
    }

    private static EnrichedIncomingDataRecord incomingDataRecordFromJson(ObjectNode value) throws Exception {
        return EnrichedIncomingDataRecord.fromJson(value);
    }

    private static String msisdnInIncomingDataRecord(EnrichedIncomingDataRecord value) throws Exception {
        return value.getMsisdn();
    }

    private static Properties kafkaConsumerProperties() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", getKafkaBootstrapServers());
        properties.setProperty("group.id", KAFKA_CONSUMER_GROUP);
        return properties;
    }

    private static String agreementIdInDataRecord(DataRecord value) throws Exception {
        return value.getAgreementId().toString();
    }

    private static FlinkKafkaProducer<DataRecordAggregate> kafkaProducer() {
        return new FlinkKafkaProducer<>(
                DATA_RECORD_AGGREGATES_TOPIC,
                IncomingDataRecordsIngester::toProducerRecord,
                kafkaProducerProperties(),
                EXACTLY_ONCE);
    }

    private static ProducerRecord<byte[], byte[]> toProducerRecord(DataRecordAggregate dataRecordAggregate, Long timestamp) {
        return new ProducerRecord<>(
                DATA_RECORD_AGGREGATES_TOPIC,
                null,
                timestamp,
                dataRecordAggregate.getAgreementId().toString().getBytes(UTF_8),
                dataRecordAggregate.toJson(objectMapper).getBytes(UTF_8)
        );
    }

    private static Properties kafkaProducerProperties() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", getKafkaBootstrapServers());
        return properties;
    }
}
