package com.hclc.mobilecs.backend.incomingdatarecords;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

import static java.time.format.DateTimeFormatter.ISO_DATE;

class IncomingDataRecordsFile implements Consumer<IncomingDataRecord> {
    private final Path dir;
    private final String prefix;
    private final String suffix;
    private String targetFileName;
    private String tmpFileName;
    private BufferedWriter bufferedWriter;

    IncomingDataRecordsFile(Path dir, String prefix, String suffix) {
        this.dir = dir;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public void accept(IncomingDataRecord incomingDataRecord) {
        try {
            if (bufferedWriter == null) {
                initializeBufferedWriter(incomingDataRecord);
            }
            bufferedWriter.write(incomingDataRecord.toCsv());
            bufferedWriter.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeBufferedWriter(IncomingDataRecord incomingDataRecord) throws IOException {
        String formattedTimestamp = incomingDataRecord.getRecordedAt().toLocalDate().format(ISO_DATE);
        this.targetFileName = prefix + formattedTimestamp + suffix;
        this.tmpFileName = "tmp_" + UUID.randomUUID().toString();
        FileWriter fileWriter = new FileWriter(dir.resolve(tmpFileName).toFile(), false);
        this.bufferedWriter = new BufferedWriter(fileWriter);
    }

    void close() {
        try {
            bufferedWriter.close();
            Files.move(dir.resolve(tmpFileName), dir.resolve(targetFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
