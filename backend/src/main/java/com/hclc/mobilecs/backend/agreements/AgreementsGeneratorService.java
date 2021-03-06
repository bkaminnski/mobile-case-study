package com.hclc.mobilecs.backend.agreements;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.hclc.mobilecs.backend.agreements.AgreementsGenerator.BILLING_PERIOD_TIME_ZONE;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toCollection;

@Component
class AgreementsGeneratorService {
    private final AgreementsGenerator agreementsGenerator;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    AgreementsGeneratorService(Environment env, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.agreementsGenerator = new AgreementsGenerator(
                parseInt(env.getProperty("mobilecs.generator.batch-size"))
        );
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    String generate() {
        List<Agreement> agreements = agreementsGenerator.generate();
        agreements.forEach(this::sendToKafka);
        applyFlinkWorkaroundForConnectedStreams();
        return summarized(agreements);
    }

    private void applyFlinkWorkaroundForConnectedStreams() {
        sendToKafka(agreementInFarFuture());
    }

    private Agreement agreementInFarFuture() {
        return new Agreement(
                UUID.randomUUID(),
                "48000000000",
                ZonedDateTime.parse("2999-01-01T00:00:00+01:00[Europe/Warsaw]"),
                ZonedDateTime.parse("2999-01-01T00:00:00+01:00[Europe/Warsaw]"),
                BILLING_PERIOD_TIME_ZONE,
                0
        );
    }

    private void sendToKafka(Agreement agreement) {
        try {
            trySendingToKafka(agreement);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void trySendingToKafka(Agreement agreement) throws InterruptedException, ExecutionException {
        kafkaTemplate.send(
                "agreements",
                null,
                agreement.getSignedAt().toInstant().toEpochMilli(),
                agreement.getMsisdn(),
                agreement.toJson(objectMapper)
        ).get();
    }

    private String summarized(List<Agreement> agreements) {
        return "Total number of generated agreements: " + agreements.size() + "\nAgreements generated in order: " + String.join(", ", agreements.stream()
                .map(Agreement::getId)
                .map(UUID::toString)
                .collect(toCollection(LinkedHashSet::new)));
    }
}
