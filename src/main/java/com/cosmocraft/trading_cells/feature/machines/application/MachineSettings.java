package com.cosmocraft.trading_cells.feature.machines.application;

import com.cosmocraft.trading_cells.feature.machines.application.port.output.MachineSettingsPort;
import java.util.Objects;

public final class MachineSettings {
    private static volatile MachineSettingsPort values = new Defaults();

    private MachineSettings() {
    }

    public static void configure(MachineSettingsPort settings) {
        values = Objects.requireNonNull(settings);
    }

    public static MachineSettingsPort values() {
        return values;
    }

    private static final class Defaults implements MachineSettingsPort {
        @Override public int villagerBreederTicks() { return 6_000; }
        @Override public int piglinBreederTicks() { return 6_000; }
        @Override public int villagerIncubatorTicks() { return 3_000; }
        @Override public int piglinIncubatorTicks() { return 6_000; }
        @Override public int farmerGrowthTicks() { return 1_200; }
        @Override public int farmerEfficiencyBonusPerLevel() { return 1; }
        @Override public int ironFarmCycleTicks() { return 1_400; }
        @Override public int ironFarmOneVillagerMultiplier() { return 1; }
        @Override public int ironFarmTwoVillagerMultiplier() { return 4; }
        @Override public int ironFarmThreeVillagerMultiplier() { return 8; }
        @Override public int ironFarmBaseIron() { return 1; }
        @Override public int ironFarmMaximumPoppies() { return 2; }
        @Override public int ironGolemAttackTicks() { return 80; }
        @Override public int ironGolemHitInterval() { return 16; }
        @Override public int ironGolemRedFlashTicks() { return 5; }
        @Override public int converterInfectionTicks() { return 100; }
        @Override public int converterCureTicks() { return 3_600; }
        @Override public int converterCureDiscountPerCycle() { return 5; }
        @Override public int converterMaximumCureDiscount() { return 25; }
        @Override public int piglinBarterTicks() { return 20; }
        @Override public int villagerTradeRefreshTicks() { return 12_000; }
        @Override public int autotraderMinimumExperience() { return 3; }
        @Override public int autotraderMaximumExperience() { return 6; }
        @Override public int autotraderLevelUpExperienceBonus() { return 5; }
        @Override public int villagerBreadCost() { return 3; }
        @Override public int villagerVegetableCost() { return 12; }
        @Override public int piglinPorkCost() { return 2; }
        @Override public int piglinCrimsonFungusCost() { return 4; }
        @Override public int maximumPendingBabies() { return 64; }
    }
}
