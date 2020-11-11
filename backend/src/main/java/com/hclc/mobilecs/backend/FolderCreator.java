package com.hclc.mobilecs.backend;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectories;

@Component
class FolderCreator implements ApplicationListener<ContextRefreshedEvent> {
    private final String dir;

    FolderCreator(Environment env) throws IOException {
        dir = env.getProperty("mobilecs.incoming-data-records-dir");
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            createDirectories(Path.of(dir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
