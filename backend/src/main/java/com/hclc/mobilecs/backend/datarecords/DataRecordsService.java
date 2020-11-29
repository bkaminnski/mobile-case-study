package com.hclc.mobilecs.backend.datarecords;

import com.hclc.mobilecs.backend.agreements.Agreement;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static com.hclc.mobilecs.backend.agreements.AgreementsGenerator.BILLING_PERIOD_TIME_ZONE;

@Component
class DataRecordsService {
    public static final BigDecimal BY_1024 = new BigDecimal(1024);
    private final DataRecordRepository dataRecordRepository;
    private final CqlTemplate cqlTemplate;

    DataRecordsService(DataRecordRepository dataRecordRepository, CqlTemplate cqlTemplate) {
        this.dataRecordRepository = dataRecordRepository;
        this.cqlTemplate = cqlTemplate;
    }

    List<DataRecord> find(@PathVariable String agreementId, @PathVariable short year, @PathVariable byte month) {
        return dataRecordRepository.findAllByKeyAgreementIdAndKeyYearAndKeyMonth(UUID.fromString(agreementId), year, month);
    }

    DataRecord upsert(@PathVariable String agreementId, @RequestBody DataRecordUpsertRequest upsertRequest) {
        Agreement agreement = new Agreement(UUID.fromString(agreementId), null, null, null, BILLING_PERIOD_TIME_ZONE, 0); // only for case study; get from agreements repository otherwise
        DataRecord dataRecord = DataRecord.from(agreement, upsertRequest);
        dataRecordRepository.save(dataRecord);
        return dataRecord;
    }

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