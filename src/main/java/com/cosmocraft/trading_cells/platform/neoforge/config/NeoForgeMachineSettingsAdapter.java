package com.cosmocraft.trading_cells.platform.neoforge.config;

import com.cosmocraft.trading_cells.feature.machines.application.port.output.MachineSettingsPort;
import com.cosmocraft.trading_cells.platform.neoforge.bootstrap.Config;

public final class NeoForgeMachineSettingsAdapter implements MachineSettingsPort {
    @Override public int villagerBreederTicks() { return Config.VILLAGER_BREEDER_TICKS.get(); }
    @Override public int piglinBreederTicks() { return Config.PIGLIN_BREEDER_TICKS.get(); }
    @Override public int villagerIncubatorTicks() { return Config.VILLAGER_INCUBATOR_TICKS.get(); }
    @Override public int piglinIncubatorTicks() { return Config.PIGLIN_INCUBATOR_TICKS.get(); }
    @Override public int farmerGrowthTicks() { return Config.FARMER_GROWTH_TICKS.get(); }
    @Override public int farmerEfficiencyBonusPerLevel() { return Config.FARMER_EFFICIENCY_BONUS_PER_LEVEL.get(); }
    @Override public int ironFarmCycleTicks() { return Config.IRON_FARM_CYCLE_TICKS.get(); }
    @Override public int ironFarmOneVillagerMultiplier() { return Config.IRON_FARM_ONE_VILLAGER_MULTIPLIER.get(); }
    @Override public int ironFarmTwoVillagerMultiplier() { return Config.IRON_FARM_TWO_VILLAGER_MULTIPLIER.get(); }
    @Override public int ironFarmThreeVillagerMultiplier() { return Config.IRON_FARM_THREE_VILLAGER_MULTIPLIER.get(); }
    @Override public int ironFarmBaseIron() { return Config.IRON_FARM_BASE_IRON.get(); }
    @Override public int ironFarmMaximumPoppies() { return Config.IRON_FARM_MAXIMUM_POPPIES.get(); }
    @Override public int ironGolemAttackTicks() { return Config.IRON_GOLEM_ATTACK_TICKS.get(); }
    @Override public int ironGolemHitInterval() { return Config.IRON_GOLEM_HIT_INTERVAL.get(); }
    @Override public int ironGolemRedFlashTicks() { return Config.IRON_GOLEM_RED_FLASH_TICKS.get(); }
    @Override public int converterInfectionTicks() { return Config.CONVERTER_INFECTION_TICKS.get(); }
    @Override public int converterCureTicks() { return Config.CONVERTER_CURE_TICKS.get(); }
    @Override public int converterCureDiscountPerCycle() { return Config.CONVERTER_CURE_DISCOUNT_PER_CYCLE.get(); }
    @Override public int converterMaximumCureDiscount() { return Config.CONVERTER_MAXIMUM_CURE_DISCOUNT.get(); }
    @Override public int piglinBarterTicks() { return Config.PIGLIN_BARTER_TICKS.get(); }
    @Override public int villagerTradeRefreshTicks() { return Config.VILLAGER_TRADE_REFRESH_TICKS.get(); }
    @Override public int autotraderMinimumExperience() { return Config.AUTOTRADER_MINIMUM_EXPERIENCE.get(); }
    @Override public int autotraderMaximumExperience() { return Config.AUTOTRADER_MAXIMUM_EXPERIENCE.get(); }
    @Override public int autotraderLevelUpExperienceBonus() { return Config.AUTOTRADER_LEVEL_UP_EXPERIENCE_BONUS.get(); }
    @Override public int villagerBreadCost() { return Config.VILLAGER_BREAD_COST.get(); }
    @Override public int villagerVegetableCost() { return Config.VILLAGER_VEGETABLE_COST.get(); }
    @Override public int piglinPorkCost() { return Config.PIGLIN_PORK_COST.get(); }
    @Override public int piglinCrimsonFungusCost() { return Config.PIGLIN_CRIMSON_FUNGUS_COST.get(); }
    @Override public int maximumPendingBabies() { return Config.MAXIMUM_PENDING_BABIES.get(); }
}
