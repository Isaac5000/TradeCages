package com.example.examplemod.feature.tradecages.domain.model;

public record TradingCellConfig(
        int maxVillagers,
        int minCaptureDistance,
        int maxCaptureDistance,
        boolean requiresShiftClick
) {
}

