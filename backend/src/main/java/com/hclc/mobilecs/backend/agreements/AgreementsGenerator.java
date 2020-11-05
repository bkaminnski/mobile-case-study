package com.hclc.mobilecs.backend.agreements;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.hclc.mobilecs.backend.UuidGenerator.nextUuid;
import static java.util.stream.Collectors.toList;

public class AgreementsGenerator {
    public static final ZonedDateTime SERVICE_START_AT = ZonedDateTime.parse("2020-01-01T00:00:00+01:00[Europe/Warsaw]");
    public static final long AGREEMENT_ID_SEED = 31423434;
    public static final long MSISDN_SEED = 84744291;
    private static final long SIGNED_AT_SEED = 55701923;
    private static final long PLAN_SEED = 73244573;
    private static final List<Long> MAX_BYTES_PLANS = List.of(
            5 * 1024 * 1024 * 1024L, // 5 GB
            10 * 1024 * 1024 * 1024L // 10 GB
    );
    private final Random agreementIdRandom = new Random(AGREEMENT_ID_SEED);
    private final Random signedAtRandom = new Random(SIGNED_AT_SEED);
    private final Random msisdnRandom = new Random(MSISDN_SEED);
    private final Random planRandom = new Random(PLAN_SEED);
    private final int numberOfAgreements;

    AgreementsGenerator(int numberOfAgreements) {
        this.numberOfAgreements = numberOfAgreements;
    }

    List<Agreement> generate() {
        return IntStream.range(0, numberOfAgreements)
                .mapToObj(this::generateAgreement)
                .collect(toList());
    }

    private Agreement generateAgreement(int i) {
        return new Agreement(
                nextUuid(agreementIdRandom),
                MsisdnValueGenerator.nextMsisdnValue(msisdnRandom),
                signedAt(),
                SERVICE_START_AT,
                "Europe/Warsaw",
                randomMaxBytesInBillingPeriod()
        );
    }

    private ZonedDateTime signedAt() {
        return SERVICE_START_AT.minusDays((long) (20 * signedAtRandom.nextDouble())).minusDays(10);
    }

    private Long randomMaxBytesInBillingPeriod() {
        return MAX_BYTES_PLANS.get((int) (planRandom.nextDouble() * MAX_BYTES_PLANS.size()));
    }
}
