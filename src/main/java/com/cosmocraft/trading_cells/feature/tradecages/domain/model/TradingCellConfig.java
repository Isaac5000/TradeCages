package com.cosmocraft.trading_cells.feature.tradecages.domain.model;

public record TradingCellConfig(
        int maxVillagers,
        int minCaptureDistance,
        int maxCaptureDistance,
        boolean requiresShiftClick
) {
}

