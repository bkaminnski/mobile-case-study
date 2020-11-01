package com.hclc.mobilecs.flink;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MatchingFunction extends RichCoFlatMapFunction<Agreement, EnrichedIncomingDataRecord, DataRecord> {
    private transient ListState<Agreement> agreements;
    private transient ListState<EnrichedIncomingDataRecord> incomingDataRecords;

    @Override
    public void open(Configuration config) throws Exception {
        agreements = getRuntimeContext().getListState(new ListStateDescriptor<>("agreements", Agreement.class));
        incomingDataRecords = getRuntimeContext().getListState(new ListStateDescriptor<>("incomingDataRecords", EnrichedIncomingDataRecord.class));
    }

    @Override
    public void flatMap1(Agreement agreement, Collector<DataRecord> out) throws Exception {
        List<Agreement> sortedByServiceStartLatestFirst = updateAgreements(agreement);
        letMatchingRecordsGo(out, sortedByServiceStartLatestFirst);
    }

    private void letMatchingRecordsGo(Collector<DataRecord> out, List<Agreement> sortedByServiceStartLatestFirst) throws Exception {
        Iterator<EnrichedIncomingDataRecord> incomingDataRecords = this.incomingDataRecords.get().iterator();
        while (incomingDataRecords.hasNext()) {
            EnrichedIncomingDataRecord incomingDataRecord = incomingDataRecords.next();
            DataRecord dataRecord = withMatchingAgreement(incomingDataRecord, sortedByServiceStartLatestFirst);
            if (dataRecord != null) {
                out.collect(dataRecord);
                incomingDataRecords.remove();
            }
        }
    }

    private List<Agreement> updateAgreements(Agreement agreement) throws Exception {
        List<Agreement> sortedByServiceStartLatestFirst = new ArrayList<>();
        this.agreements.get().forEach(sortedByServiceStartLatestFirst::add);
        sortedByServiceStartLatestFirst.add(agreement);
        Collections.sort(sortedByServiceStartLatestFirst);
        this.agreements.update(sortedByServiceStartLatestFirst);
        return sortedByServiceStartLatestFirst;
    }

    @Override
    public void flatMap2(EnrichedIncomingDataRecord incomingDataRecord, Collector<DataRecord> out) throws Exception {
        Iterable<Agreement> sortedByServiceStartLatestFirst = this.agreements.get();
        if (sortedByServiceStartLatestFirst == null) {
            this.incomingDataRecords.add(incomingDataRecord);
            return;
        }
        DataRecord toLetGo = withMatchingAgreement(incomingDataRecord, sortedByServiceStartLatestFirst);
        if (toLetGo != null) {
            out.collect(toLetGo);
        } else {
            this.incomingDataRecords.add(incomingDataRecord);
        }
    }

    private DataRecord withMatchingAgreement(EnrichedIncomingDataRecord incomingDataRecord, Iterable<Agreement> sortedByServiceStartLatestFirst) throws Exception {
        for (Agreement agreement : sortedByServiceStartLatestFirst) {
            if (agreement.getServiceStartAt().isBefore(incomingDataRecord.getRecordedAt())) {
                return new DataRecord(agreement, incomingDataRecord);
            }
        }
        return null;
    }
}
