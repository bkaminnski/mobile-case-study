package com.hclc.mobilecs.backend.datarecords;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/data-records")
class DataRecordsController {
    private final DataRecordsService dataRecordsService;

    DataRecordsController(DataRecordsService dataRecordsService) {
        this.dataRecordsService = dataRecordsService;
    }

    @GetMapping("/{agreementId}/{year}/{month}")
    @CrossOrigin(origins = "http://localhost:3000")
    List<DataRecord> find(@PathVariable String agreementId, @PathVariable short year, @PathVariable byte month) {
        return dataRecordsService.find(agreementId, year, month);
    }

    @PutMapping("/{agreementId}")
    @CrossOrigin(origins = "http://localhost:3000")
    DataRecord upsert(@PathVariable String agreementId, @RequestBody DataRecordUpsertRequest upsertRequest) {
        return dataRecordsService.upsert(agreementId, upsertRequest);
    }

    @GetMapping("/{agreementId}/{year}/{month}/total-data-used")
    String getTotalDataUsage(@PathVariable String agreementId, @PathVariable short year, @PathVariable byte month) {
        return dataRecordsService.getTotalDataUsage(agreementId, year, month);
    }
}