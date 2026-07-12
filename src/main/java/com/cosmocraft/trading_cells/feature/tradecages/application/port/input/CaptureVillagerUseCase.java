package com.cosmocraft.trading_cells.feature.tradecages.application.port.input;

import com.cosmocraft.trading_cells.feature.tradecages.domain.model.TradeVillager;

public interface CaptureVillagerUseCase {
    boolean hasCapacity();

    boolean captureVillager(TradeVillager villager);

    int getVillagerCount();
}
