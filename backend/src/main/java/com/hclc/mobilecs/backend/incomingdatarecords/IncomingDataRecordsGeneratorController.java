package com.hclc.mobilecs.backend.incomingdatarecords;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/incoming-data-records")
class IncomingDataRecordsGeneratorController {
    private final IncomingDataRecordsGeneratorService incomingDataRecordsGeneratorService;

    IncomingDataRecordsGeneratorController(IncomingDataRecordsGeneratorService incomingDataRecordsGeneratorService) {
        this.incomingDataRecordsGeneratorService = incomingDataRecordsGeneratorService;
    }

    @GetMapping("/generate")
    String generate() {
        return incomingDataRecordsGeneratorService.generate();
    }
}