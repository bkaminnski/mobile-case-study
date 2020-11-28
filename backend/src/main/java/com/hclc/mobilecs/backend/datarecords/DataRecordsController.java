package com.hclc.mobilecs.backend.datarecords;

import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/data-records")
class DataRecordsController {
    public static final BigDecimal BY_1024 = new BigDecimal(1024);
    private final DataRecordRepository dataRecordRepository;
    private final CqlTemplate cqlTemplate;

    DataRecordsController(DataRecordRepository dataRecordRepository, CqlTemplate cqlTemplate) {
        this.dataRecordRepository = dataRecordRepository;
        this.cqlTemplate = cqlTemplate;
    }

    @GetMapping("/{agreementId}/{year}/{month}")
    @CrossOrigin(origins = "http://localhost:3000")
    List<DataRecord> find(@PathVariable String agreementId, @PathVariable short year, @PathVariable byte month) {
        return dataRecordRepository.findAllByKeyAgreementIdAndKeyYearAndKeyMonth(UUID.fromString(agreementId), year, month);
    }

    @GetMapping("/{agreementId}/{year}/{month}/total-data-used")
    String getTotalDataUsage(@PathVariable String agreementId, @PathVariable short year, @PathVariable byte month) {
        BigDecimal bytes = new BigDecimal(cqlTemplate.queryForObject(
                "select sum(recorded_bytes) from data_record where agreement_id = ? and year = ? and month = ?",
                Long.class,
                UUID.fromString(agreementId),
                year,
                month
        ));
        return bytes + " B\n"
                + bytes.divide(BY_1024, 0, RoundingMode.HALF_UP) + " KB\n"
                + bytes.divide(BY_1024.pow(2), 0, RoundingMode.HALF_UP) + " MB\n"
                + bytes.divide(BY_1024.pow(3), 0, RoundingMode.HALF_UP) + " GB";
    }
}