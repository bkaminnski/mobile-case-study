package com.hclc.mobilecs.backend.incomingdatarecords;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import static java.time.format.DateTimeFormatter.ISO_DATE;

class IncomingDataRecordsFile implements Consumer<IncomingDataRecord> {
    private final Path dir;
    private final String prefix;
    private final String suffix;
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
        String fileName = prefix + formattedTimestamp + suffix;
        FileWriter fileWriter = new FileWriter(dir.resolve(fileName).toFile(), false);
        this.bufferedWriter = new BufferedWriter(fileWriter);
    }

    void close() {
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
