package com.example.examplemod.feature.tradecages.domain.model;

import java.util.UUID;

public record TradeVillager(
        UUID villageId,
        String villagerType,
        int experience,
        long captureTime
) {
}

