package com.hclc.mobile.backend.datarecords;

import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/data-records")
class DataRecordsController {
    private final DataRecordRepository dataRecordRepository;
    private final CqlTemplate cqlTemplate;

    DataRecordsController(DataRecordRepository dataRecordRepository, CqlTemplate cqlTemplate) {
        this.dataRecordRepository = dataRecordRepository;
        this.cqlTemplate = cqlTemplate;
    }

    @GetMapping("/{agreementId}/{year}/{month}")
    List<DataRecord> find(@PathVariable String agreementId, @PathVariable short year, @PathVariable byte month) {
        return dataRecordRepository.findAllByKeyAgreementIdAndKeyYearAndKeyMonth(UUID.fromString(agreementId), year, month);
    }

    @GetMapping("/{agreementId}/{year}/{month}/total-data-used")
    Long getTotalDataUsage(@PathVariable String agreementId, @PathVariable short year, @PathVariable byte month) {
        return cqlTemplate.queryForObject(
                "select sum(recorded_bytes) from data_record where agreement_id = ? and year = ? and month = ?",
                Long.class,
                UUID.fromString(agreementId),
                year,
                month
        );
    }
}