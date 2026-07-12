package com.cosmocraft.trading_cells.feature.tradecages.application.port.input;

public interface ReleaseVillagerUseCase {
    void releaseVillager(int index);
    int getStoredVillagerCount();
}

