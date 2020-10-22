package com.hclc.mobilecs.backend.datarecords;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static java.lang.String.join;
import static java.util.stream.Collectors.toCollection;

@Component
class DataRecordsGeneratorService {
    private final DataRecordRepository dataRecordRepository;

    DataRecordsGeneratorService(DataRecordRepository dataRecordRepository) {
        this.dataRecordRepository = dataRecordRepository;
    }

    String generate() {
        List<DataRecord> records = new DataRecordsGenerator(1, 1).generate();
        dataRecordRepository.saveAll(records);
        return summarized(records);
    }

    private String summarized(List<DataRecord> records) {
        return "Total number of generated records: " + records.size() + "\nAgreements generated in order: " + String.join(", ", records.stream()
                .map(DataRecord::getKey)
                .map(DataRecordKey::getAgreementId)
                .map(UUID::toString)
                .collect(toCollection(LinkedHashSet::new)));
    }
}
