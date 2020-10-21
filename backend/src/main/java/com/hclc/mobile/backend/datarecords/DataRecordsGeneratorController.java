package com.hclc.mobile.backend.datarecords;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/data-records")
class DataRecordsGeneratorController {
    private final DataRecordsGeneratorService dataRecordsGeneratorService;

    DataRecordsGeneratorController(DataRecordsGeneratorService dataRecordsGeneratorService) {
        this.dataRecordsGeneratorService = dataRecordsGeneratorService;
    }

    @GetMapping("/generate")
    String generate() {
        return dataRecordsGeneratorService.generate();
    }
}