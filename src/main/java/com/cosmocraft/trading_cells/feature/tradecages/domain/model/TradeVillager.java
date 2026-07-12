package com.cosmocraft.trading_cells.feature.tradecages.domain.model;

import java.util.UUID;

public record TradeVillager(
        UUID villagerId,
        String villagerType,
        int experience,
        long captureTime
) {
}
