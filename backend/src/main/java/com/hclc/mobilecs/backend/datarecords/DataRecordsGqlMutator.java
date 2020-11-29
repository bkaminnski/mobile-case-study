package com.hclc.mobilecs.backend.datarecords;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import org.springframework.stereotype.Component;

@Component
class DataRecordsGqlMutator implements GraphQLMutationResolver {
    private final DataRecordsService dataRecordsService;

    DataRecordsGqlMutator(DataRecordsService dataRecordsService) {
        this.dataRecordsService = dataRecordsService;
    }

    public DataRecord upsertDataRecord(String agreementId, DataRecordUpsertRequest upsertRequest) {
        return dataRecordsService.upsert(agreementId, upsertRequest);
    }
}
