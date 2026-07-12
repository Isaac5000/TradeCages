package com.cosmocraft.trading_cells.feature.tradecages.domain.model;

import com.cosmocraft.trading_cells.feature.machines.application.MachineSettings;

public final class VillagerOfferPersistence {
    public static final int MINIMUM_REFRESH_TICKS = 12_000;

    private VillagerOfferPersistence() {
    }

    public static int refreshIntervalTicks() {
        return Math.max(MINIMUM_REFRESH_TICKS, MachineSettings.values().villagerTradeRefreshTicks());
    }
}
