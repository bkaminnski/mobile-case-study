package com.hclc.mobilecs.flink;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Fixtures {
    public static final String IRRELEVANT_STRING = "IRRELEVANT";
    public static final ZonedDateTime IRRELEVANT_TIMESTAMP = ZonedDateTime.parse("2020-01-01T00:00:00+01:00[Europe/Warsaw]");
    public static final long IRRELEVANT_LONG = 4124L;
    public static final UUID IRRELEVANT_UUID = UUID.fromString("ca9681a2-d5fd-47bc-91bc-34ca6d26b6c5");
    public static final ObjectMapper objectMapper = new ObjectMapper();
}
