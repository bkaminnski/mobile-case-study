package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeutils.base.LongSerializer;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import static java.lang.Boolean.TRUE;
import static org.apache.flink.streaming.api.windowing.triggers.TriggerResult.CONTINUE;
import static org.apache.flink.streaming.api.windowing.triggers.TriggerResult.FIRE;

public class DataPlanTrigger extends Trigger<DataRecord, TimeWindow> {
    private final ReducingStateDescriptor<Long> recordedBytesDescriptor = new ReducingStateDescriptor<>(
            "recordedBytes",
            Long::sum,
            LongSerializer.INSTANCE
    );
    private final ValueStateDescriptor<Boolean> alreadyFiredDescriptor = new ValueStateDescriptor<>(
            "alreadyFiredDueToExceededPlan", Boolean.class
    );

    @Override
    public TriggerResult onElement(DataRecord element, long timestamp, TimeWindow window, TriggerContext ctx) throws Exception {
        if (window.maxTimestamp() <= ctx.getCurrentWatermark()) {
            // if the watermark is already past the window fire immediately
            return FIRE;
        }
        // make sure to always fire at the end of the window (regardless of data usage exceeded)
        ctx.registerEventTimeTimer(window.maxTimestamp());

        ReducingState<Long> recordedBytes = ctx.getPartitionedState(recordedBytesDescriptor);
        recordedBytes.add(element.getRecordedBytes());
        ValueState<Boolean> alreadyFired = ctx.getPartitionedState(alreadyFiredDescriptor);
        if (recordedBytes.get() >= element.getMaxBytesInBillingPeriod() && (alreadyFired.value() == null || !alreadyFired.value())) {
            // when data usage exceeded fire only on the first record
            alreadyFired.update(TRUE);
            // fire if data usage exceeds the plan
            return FIRE;
        }
        return CONTINUE;
    }

    @Override
    public TriggerResult onProcessingTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
        return CONTINUE;
    }

    @Override
    public TriggerResult onEventTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
        return time == window.maxTimestamp() ? FIRE : CONTINUE;
    }

    @Override
    public void clear(TimeWindow window, TriggerContext ctx) throws Exception {
        ctx.deleteEventTimeTimer(window.maxTimestamp());
        ctx.getPartitionedState(recordedBytesDescriptor).clear();
        ctx.getPartitionedState(alreadyFiredDescriptor).clear();
    }
}
