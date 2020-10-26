package com.hclc.mobilecs.backend.incomingdatarecords;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
class IncomingDataRecordsGeneratorService {
    private final IncomingDataRecordsFilesCreator filesCreator;
    private final IncomingDataRecordsGenerator recordsGenerator;

    IncomingDataRecordsGeneratorService(Environment env) {
        this.filesCreator = new IncomingDataRecordsFilesCreator(env.getProperty("mobilecs.incoming-data-records-dir"));
        this.recordsGenerator = new IncomingDataRecordsGenerator(5, 1);
    }

    String generate() {
        Set<String> msisdns = new HashSet<>();
        IncomingDataRecordsFile incomingDataRecordsFile = filesCreator.next();
        recordsGenerator.generateTo(incomingDataRecordsFile.andThen(i -> msisdns.add(i.getMsisdn())));
        incomingDataRecordsFile.close();
        return "MSISDNs generated: " + String.join(", ", msisdns);
    }
}
