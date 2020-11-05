package com.hclc.mobilecs.flink;

import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Collection;

import static com.hclc.mobilecs.flink.DataRecordTestBuilder.aDataRecord;
import static org.assertj.core.api.Assertions.assertThat;

class BillingPeriodWindowAssignerTest {
    private static final String TIME_ZONE = "Europe/Warsaw";
    private static final long EVENT_TIME = ZonedDateTime.parse("2020-01-07T11:47:32+01:00[Europe/Warsaw]").toInstant().toEpochMilli();
    private static final long EXPECTED_WINDOW_START = ZonedDateTime.parse("2020-01-01T00:00:00+01:00[Europe/Warsaw]").toInstant().toEpochMilli();
    private static final long EXPECTED_WINDOW_END = ZonedDateTime.parse("2020-02-01T00:00:00+01:00[Europe/Warsaw]").toInstant().toEpochMilli();

    @Test
    void shouldAssignToCalendarMonthWindow() {
        BillingPeriodWindowAssigner windowAssigner = new BillingPeriodWindowAssigner();

        Collection<TimeWindow> actualTimeWindows = windowAssigner.assignWindows(arrangeDataRecord(), EVENT_TIME, null);

        assertThat(actualTimeWindows)
                .hasSize(1)
                .first().isEqualTo(expectedTimeWindow());
    }

    private DataRecord arrangeDataRecord() {
        return aDataRecord().withBillingPeriodTimeZone(TIME_ZONE).build();
    }

    private TimeWindow expectedTimeWindow() {
        return new TimeWindow(EXPECTED_WINDOW_START, EXPECTED_WINDOW_END);
    }
}