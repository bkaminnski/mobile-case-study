package com.hclc.mobilecs.flink;

import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;

public class Configuration {
    public static String getKafkaBootstrapServers() {
        return ofNullable(getenv("KAFKA_BOOTSTRAP_SERVERS")).orElse("127.0.0.1:9092");
    }

    public static String getCassandraContactPoint() {
        return ofNullable(getenv("CASSANDRA_CONTACT_POINT")).orElse("127.0.0.1");
    }

    public static String getMobilecsIncomingDataRecordsDir() {
        return ofNullable(getenv("MOBILECS_INCOMING_DATA_RECORDS_DIR")).orElse("/tmp/incoming-data-records");
    }
}
