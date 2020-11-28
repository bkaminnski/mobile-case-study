package com.hclc.mobilecs.backend.datarecords;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toCollection;

@Component
class DataRecordsGeneratorService {
    private final DataRecordsGenerator dataRecordsGenerator;
    private final DataRecordRepository dataRecordRepository;

    DataRecordsGeneratorService(Environment env, DataRecordRepository dataRecordRepository) {
        this.dataRecordsGenerator = new DataRecordsGenerator(
                parseInt(env.getProperty("mobilecs.generator.batch-size")),
                parseInt(env.getProperty("mobilecs.generator.incoming-data-records-months"))
        );
        this.dataRecordRepository = dataRecordRepository;
    }

    String generate() {
        List<DataRecord> records = dataRecordsGenerator.generate();
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
