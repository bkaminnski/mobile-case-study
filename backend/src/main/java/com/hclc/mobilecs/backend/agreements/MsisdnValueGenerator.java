package com.hclc.mobilecs.backend.agreements;

import java.util.Random;

public class MsisdnValueGenerator {
    public static String nextMsisdnValue(Random random) {
        return "48" + (long) (random.nextDouble() * 1000_000_000L);
    }
}
