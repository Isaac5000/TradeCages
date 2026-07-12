package com.cosmocraft.trading_cells.feature.breeders.domain.model;

import com.cosmocraft.trading_cells.feature.machines.application.MachineSettings;

public final class BreederRecipe {
    private BreederRecipe() {
    }

    public static boolean isFood(BreederKind kind, BreederFood food) {
        return switch (kind) {
            case VILLAGER -> food == BreederFood.BREAD || food == BreederFood.VEGETABLE;
            case PIGLIN -> food == BreederFood.PORK || food == BreederFood.CRIMSON_FUNGUS;
        };
    }

    public static int cost(BreederKind kind, BreederFood food) {
        if (!isFood(kind, food)) {
            return Integer.MAX_VALUE;
        }
        return switch (kind) {
            case VILLAGER -> food == BreederFood.BREAD
                    ? MachineSettings.values().villagerBreadCost()
                    : MachineSettings.values().villagerVegetableCost();
            case PIGLIN -> food == BreederFood.PORK
                    ? MachineSettings.values().piglinPorkCost()
                    : MachineSettings.values().piglinCrimsonFungusCost();
        };
    }

    public static int breedTicks(BreederKind kind) {
        return kind == BreederKind.VILLAGER
                ? MachineSettings.values().villagerBreederTicks()
                : MachineSettings.values().piglinBreederTicks();
    }
}
