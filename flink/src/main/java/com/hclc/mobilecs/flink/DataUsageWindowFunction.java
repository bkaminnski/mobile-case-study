package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import static com.hclc.mobilecs.flink.DataRecordAggregate.AggregateType.BILLING_PERIOD_CLOSED;
import static com.hclc.mobilecs.flink.DataRecordAggregate.AggregateType.DATA_PLAN_EXCEEDED;
import static java.lang.Boolean.TRUE;

public class DataUsageWindowFunction extends ProcessWindowFunction<DataRecord, DataRecordAggregate, String, TimeWindow> {
    private final ValueStateDescriptor<Boolean> alreadyFiredDescriptor = new ValueStateDescriptor<>(
            "alreadyFiredDueToExceededPlan", Boolean.class
    );

    @Override
    public void process(String key, Context context, Iterable<DataRecord> elements, Collector<DataRecordAggregate> out) throws Exception {
        DataRecord latestDataRecordAggregatingRecordedBytes = elements.iterator().next();
        if (latestDataRecordAggregatingRecordedBytes == null) {
            return;
        }
        ValueState<Boolean> alreadyFired = context.windowState().getState(alreadyFiredDescriptor);
        if (latestDataRecordAggregatingRecordedBytes.exceedsDataPlan() && (alreadyFired.value() == null && !alreadyFired.value())) {
            alreadyFired.update(TRUE);
            out.collect(new DataRecordAggregate(latestDataRecordAggregatingRecordedBytes, DATA_PLAN_EXCEEDED));
        }
        if (context.currentWatermark() >= context.window().maxTimestamp()) {
            out.collect(new DataRecordAggregate(latestDataRecordAggregatingRecordedBytes, BILLING_PERIOD_CLOSED));
        }
    }

    @Override
    public void clear(Context context) throws Exception {
        super.clear(context);
    }
}
