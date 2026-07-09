package com.example.examplemod.feature.milkcookie.domain.model;

public record MilkCookieFoodSpec(
        int nutrition,
        float saturationModifier,
        int nauseaDurationTicks,
        int hungerDurationTicks,
        float hungerChance
) {
    public static final int TICKS_PER_SECOND = 20;

    public static MilkCookieFoodSpec standard() {
        return new MilkCookieFoodSpec(
                1,
                1.5F,
                5 * TICKS_PER_SECOND,
                5 * TICKS_PER_SECOND,
                0.8F
        );
    }
}
