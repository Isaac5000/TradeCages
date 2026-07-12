package com.cosmocraft.trading_cells.feature.farmer.domain.model;

import com.cosmocraft.trading_cells.feature.machines.application.MachineSettings;

public final class FarmerCycle {
    private FarmerCycle() {
    }

    public static int growthTicks() {
        return MachineSettings.values().farmerGrowthTicks();
    }

    public static FarmerHarvest harvest(FarmerCrop crop, int fortuneLevel) {
        int fortune = Math.max(0, fortuneLevel);
        return switch (crop) {
            case WHEAT, BEETROOT -> new FarmerHarvest(1, 1 + fortune);
            case CARROT, POTATO -> new FarmerHarvest(2 + fortune, 0);
            case NONE -> new FarmerHarvest(0, 0);
        };
    }
}
