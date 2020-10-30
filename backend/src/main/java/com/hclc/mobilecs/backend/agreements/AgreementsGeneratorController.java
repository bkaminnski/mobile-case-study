package com.hclc.mobilecs.backend.agreements;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/agreements")
class AgreementsGeneratorController {
    private final AgreementsGeneratorService agreementsGeneratorService;

    AgreementsGeneratorController(AgreementsGeneratorService agreementsGeneratorService) {
        this.agreementsGeneratorService = agreementsGeneratorService;
    }

    @GetMapping("/generate")
    String generate() {
        return agreementsGeneratorService.generate();
    }
}