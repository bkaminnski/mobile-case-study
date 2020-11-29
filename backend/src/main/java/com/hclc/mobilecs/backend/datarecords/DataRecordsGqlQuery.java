package com.hclc.mobilecs.backend.datarecords;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class DataRecordsGqlQuery implements GraphQLQueryResolver {
    private final DataRecordsService dataRecordsService;

    DataRecordsGqlQuery(DataRecordsService dataRecordsService) {
        this.dataRecordsService = dataRecordsService;
    }

    public List<DataRecord> findDataRecords(String agreementId, short year, byte month) {
        return dataRecordsService.find(agreementId, year, month);
    }

    public String getTotalDataUsage(String agreementId, short year, byte month) {
        return dataRecordsService.getTotalDataUsage(agreementId, year, month);
    }
}
