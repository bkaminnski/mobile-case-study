package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;

import static java.time.temporal.ChronoUnit.DAYS;

public class BillingPeriodWindowAssigner extends WindowAssigner<DataRecord, TimeWindow> {
    @Override
    public Collection<TimeWindow> assignWindows(DataRecord element, long timestamp, WindowAssignerContext context) {
        if (timestamp > Long.MIN_VALUE) {
            ZonedDateTime zonedEventTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.of(element.getBillingPeriodTimeZone()));
            ZonedDateTime monthStart = zonedEventTime.truncatedTo(DAYS).withDayOfMonth(1);
            return Collections.singletonList(new TimeWindow(
                    monthStart.toInstant().toEpochMilli(),
                    monthStart.plusMonths(1).toInstant().toEpochMilli()
            ));
        } else {
            throw new RuntimeException("Record has Long.MIN_VALUE timestamp (= no timestamp marker). " +
                    "Is the time characteristic set to 'ProcessingTime', or did you forget to call " +
                    "'DataStream.assignTimestampsAndWatermarks(...)'?");
        }
    }

    @Override
    public Trigger<DataRecord, TimeWindow> getDefaultTrigger(StreamExecutionEnvironment env) {
        return null;
    }

    @Override
    public String toString() {
        return "BillingPeriodWindowAssigner";
    }

    @Override
    public TypeSerializer<TimeWindow> getWindowSerializer(ExecutionConfig executionConfig) {
        return new TimeWindow.Serializer();
    }

    @Override
    public boolean isEventTime() {
        return true;
    }
}
