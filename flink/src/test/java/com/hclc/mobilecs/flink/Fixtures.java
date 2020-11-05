package com.hclc.mobilecs.flink;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.util.UUID;

class Fixtures {
    static final String IRRELEVANT_STRING = "IRRELEVANT";
    static final ZonedDateTime IRRELEVANT_TIMESTAMP = ZonedDateTime.parse("2020-01-01T00:00:00+01:00[Europe/Warsaw]");
    static final long IRRELEVANT_LONG = 4124L;
    static final UUID IRRELEVANT_UUID = UUID.fromString("ca9681a2-d5fd-47bc-91bc-34ca6d26b6c5");
    static final ObjectMapper objectMapper = new ObjectMapper();
}
