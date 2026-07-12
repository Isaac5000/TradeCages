package com.cosmocraft.trading_cells.feature.machines.application.port.output;

public interface MachineSettingsPort {
    int villagerBreederTicks();

    int piglinBreederTicks();

    int villagerIncubatorTicks();

    int piglinIncubatorTicks();

    int farmerGrowthTicks();

    int farmerEfficiencyBonusPerLevel();

    int ironFarmCycleTicks();

    int ironFarmOneVillagerMultiplier();

    int ironFarmTwoVillagerMultiplier();

    int ironFarmThreeVillagerMultiplier();

    int ironFarmBaseIron();

    int ironFarmMaximumPoppies();

    int ironGolemAttackTicks();

    int ironGolemHitInterval();

    int ironGolemRedFlashTicks();

    int converterInfectionTicks();

    int converterCureTicks();

    int converterCureDiscountPerCycle();

    int converterMaximumCureDiscount();

    int piglinBarterTicks();

    int villagerTradeRefreshTicks();

    int autotraderMinimumExperience();

    int autotraderMaximumExperience();

    int autotraderLevelUpExperienceBonus();

    int villagerBreadCost();

    int villagerVegetableCost();

    int piglinPorkCost();

    int piglinCrimsonFungusCost();

    int maximumPendingBabies();
}
