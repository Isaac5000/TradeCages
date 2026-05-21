package com.example.examplemod.feature.tradecages.domain.model;

public record TradeCageConfig(
        int maxVillagers,
        int minCaptureDistance,
        int maxCaptureDistance,
        boolean requiresShiftClick
) {
}

