package com.cosmocraft.trading_cells.platform.neoforge.bootstrap;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue VILLAGER_BREEDER_TICKS;
    public static final ModConfigSpec.IntValue PIGLIN_BREEDER_TICKS;
    public static final ModConfigSpec.IntValue VILLAGER_INCUBATOR_TICKS;
    public static final ModConfigSpec.IntValue PIGLIN_INCUBATOR_TICKS;
    public static final ModConfigSpec.IntValue FARMER_GROWTH_TICKS;
    public static final ModConfigSpec.IntValue IRON_FARM_CYCLE_TICKS;
    public static final ModConfigSpec.IntValue CONVERTER_INFECTION_TICKS;
    public static final ModConfigSpec.IntValue CONVERTER_CURE_TICKS;
    public static final ModConfigSpec.IntValue PIGLIN_BARTER_TICKS;
    public static final ModConfigSpec.IntValue VILLAGER_TRADE_REFRESH_TICKS;

    public static final ModConfigSpec.IntValue FARMER_EFFICIENCY_BONUS_PER_LEVEL;
    public static final ModConfigSpec.IntValue IRON_FARM_ONE_VILLAGER_MULTIPLIER;
    public static final ModConfigSpec.IntValue IRON_FARM_TWO_VILLAGER_MULTIPLIER;
    public static final ModConfigSpec.IntValue IRON_FARM_THREE_VILLAGER_MULTIPLIER;
    public static final ModConfigSpec.IntValue IRON_FARM_BASE_IRON;
    public static final ModConfigSpec.IntValue IRON_FARM_MAXIMUM_POPPIES;
    public static final ModConfigSpec.IntValue IRON_GOLEM_ATTACK_TICKS;
    public static final ModConfigSpec.IntValue IRON_GOLEM_HIT_INTERVAL;
    public static final ModConfigSpec.IntValue IRON_GOLEM_RED_FLASH_TICKS;
    public static final ModConfigSpec.IntValue CONVERTER_CURE_DISCOUNT_PER_CYCLE;
    public static final ModConfigSpec.IntValue CONVERTER_MAXIMUM_CURE_DISCOUNT;
    public static final ModConfigSpec.IntValue AUTOTRADER_MINIMUM_EXPERIENCE;
    public static final ModConfigSpec.IntValue AUTOTRADER_MAXIMUM_EXPERIENCE;
    public static final ModConfigSpec.IntValue AUTOTRADER_LEVEL_UP_EXPERIENCE_BONUS;

    public static final ModConfigSpec.IntValue VILLAGER_BREAD_COST;
    public static final ModConfigSpec.IntValue VILLAGER_VEGETABLE_COST;
    public static final ModConfigSpec.IntValue PIGLIN_PORK_COST;
    public static final ModConfigSpec.IntValue PIGLIN_CRIMSON_FUNGUS_COST;
    public static final ModConfigSpec.IntValue MAXIMUM_PENDING_BABIES;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("Machine durations in game ticks.").push("timers");
        VILLAGER_BREEDER_TICKS = ticks("villagerBreeder", 6_000);
        PIGLIN_BREEDER_TICKS = ticks("piglinBreeder", 6_000);
        VILLAGER_INCUBATOR_TICKS = ticks("villagerIncubator", 3_000);
        PIGLIN_INCUBATOR_TICKS = ticks("piglinIncubator", 3_000);
        FARMER_GROWTH_TICKS = ticks("farmerGrowth", 1_200);
        IRON_FARM_CYCLE_TICKS = ticks("ironFarmCycle", 1_400);
        CONVERTER_INFECTION_TICKS = ticks("converterInfection", 100);
        CONVERTER_CURE_TICKS = ticks("converterCure", 3_600);
        PIGLIN_BARTER_TICKS = ticks("piglinBarter", 20);
        VILLAGER_TRADE_REFRESH_TICKS = ticks("villagerTradeRefresh", 12_000);
        BUILDER.pop();

        BUILDER.comment("Production, animation and experience tuning.").push("production");
        FARMER_EFFICIENCY_BONUS_PER_LEVEL = nonNegative("farmerEfficiencyBonusPerLevel", 1);
        IRON_FARM_ONE_VILLAGER_MULTIPLIER = positive("ironFarmOneVillagerMultiplier", 1);
        IRON_FARM_TWO_VILLAGER_MULTIPLIER = positive("ironFarmTwoVillagerMultiplier", 4);
        IRON_FARM_THREE_VILLAGER_MULTIPLIER = positive("ironFarmThreeVillagerMultiplier", 8);
        IRON_FARM_BASE_IRON = positive("ironFarmBaseIron", 1);
        IRON_FARM_MAXIMUM_POPPIES = nonNegative("ironFarmMaximumPoppies", 2);
        IRON_GOLEM_ATTACK_TICKS = positive("ironGolemAttackTicks", 80);
        IRON_GOLEM_HIT_INTERVAL = positive("ironGolemHitInterval", 16);
        IRON_GOLEM_RED_FLASH_TICKS = positive("ironGolemRedFlashTicks", 5);
        CONVERTER_CURE_DISCOUNT_PER_CYCLE = nonNegative("converterCureDiscountPerCycle", 5);
        CONVERTER_MAXIMUM_CURE_DISCOUNT = nonNegative("converterMaximumCureDiscount", 25);
        AUTOTRADER_MINIMUM_EXPERIENCE = nonNegative("autotraderMinimumExperience", 3);
        AUTOTRADER_MAXIMUM_EXPERIENCE = nonNegative("autotraderMaximumExperience", 6);
        AUTOTRADER_LEVEL_UP_EXPERIENCE_BONUS = nonNegative("autotraderLevelUpExperienceBonus", 5);
        BUILDER.pop();

        BUILDER.comment("Food consumed by each breeding cycle.").push("breedingCosts");
        VILLAGER_BREAD_COST = positive("villagerBread", 3);
        VILLAGER_VEGETABLE_COST = positive("villagerVegetable", 12);
        PIGLIN_PORK_COST = positive("piglinPork", 2);
        PIGLIN_CRIMSON_FUNGUS_COST = positive("piglinCrimsonFungus", 4);
        MAXIMUM_PENDING_BABIES = positive("maximumPendingBabies", 64);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private Config() {
    }

    private static ModConfigSpec.IntValue ticks(String name, int defaultValue) {
        return BUILDER.defineInRange(name + "Ticks", defaultValue, 1, 72_000);
    }

    private static ModConfigSpec.IntValue positive(String name, int defaultValue) {
        return BUILDER.defineInRange(name, defaultValue, 1, 1_024);
    }

    private static ModConfigSpec.IntValue nonNegative(String name, int defaultValue) {
        return BUILDER.defineInRange(name, defaultValue, 0, 1_024);
    }
}
