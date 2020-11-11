package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.io.FilePathFilter;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.ZonedDateTime;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.flink.streaming.api.TimeCharacteristic.EventTime;
import static org.apache.flink.streaming.api.functions.source.FileProcessingMode.PROCESS_CONTINUOUSLY;
import static org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer.Semantic.EXACTLY_ONCE;

public class IncomingDataRecordsImporter {
    private static final String INCOMING_DATA_RECORDS_TOPIC = "incoming-data-records";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = createExecutionEnvironment();
        readingFilesContinuously(env)
                .setParallelism(1)
                .map(IncomingDataRecordsImporter::toIncomingDataRecords)
                .map(IncomingDataRecordsImporter::toEnrichedIncomingDataRecords)
                .assignTimestampsAndWatermarks(noWatermarkWithTimestampAssigner())
                .addSink(kafkaProducer())
                .name("Kafka [" + INCOMING_DATA_RECORDS_TOPIC + "]");
        env.execute("Incoming Data Records Importer");
    }

    private static StreamExecutionEnvironment createExecutionEnvironment() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(EventTime);
        env.setParallelism(1);
        return env;
    }

    private static DataStreamSource<String> readingFilesContinuously(StreamExecutionEnvironment env) {
        String pathString = "/tmp/incoming-data-records";
        Path filePath = new Path(pathString);
        TextInputFormat textInputFormat = new TextInputFormat(filePath);
        textInputFormat.setFilesFilter(new FilePathFilter() {
            private final FilePathFilter defaultFilter = DefaultFilter.createDefaultFilter();

            @Override
            public boolean filterPath(Path filePath) {
                return defaultFilter.filterPath(filePath) || filePath.getName().startsWith("tmp");
            }
        });
        return env.readFile(textInputFormat, pathString, PROCESS_CONTINUOUSLY, 500);
    }

    private static IncomingDataRecord toIncomingDataRecords(String value) {
        String[] fields = value.split(";");
        return new IncomingDataRecord(fields[0], ZonedDateTime.parse(fields[1]), fields[2], Long.parseLong(fields[3]));
    }

    private static EnrichedIncomingDataRecord toEnrichedIncomingDataRecords(IncomingDataRecord value) {
        return new EnrichedIncomingDataRecord(value);
    }

    private static WatermarkStrategy<EnrichedIncomingDataRecord> noWatermarkWithTimestampAssigner() {
        return WatermarkStrategy.<EnrichedIncomingDataRecord>noWatermarks()
                .withTimestampAssigner((event, timestamp) -> event.getEventTimestampMillis());
    }

    private static FlinkKafkaProducer<EnrichedIncomingDataRecord> kafkaProducer() {
        return new FlinkKafkaProducer<>(
                INCOMING_DATA_RECORDS_TOPIC,
                IncomingDataRecordsImporter::toProducerRecord,
                kafkaProducerProperties(),
                EXACTLY_ONCE);
    }

    private static ProducerRecord<byte[], byte[]> toProducerRecord(EnrichedIncomingDataRecord enrichedIncomingDataRecord, Long timestamp) {
        return new ProducerRecord<>(
                INCOMING_DATA_RECORDS_TOPIC,
                null,
                timestamp,
                enrichedIncomingDataRecord.getMsisdn().getBytes(UTF_8),
                enrichedIncomingDataRecord.toJson(objectMapper).getBytes(UTF_8)
        );
    }

    private static Properties kafkaProducerProperties() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        return properties;
    }
}
