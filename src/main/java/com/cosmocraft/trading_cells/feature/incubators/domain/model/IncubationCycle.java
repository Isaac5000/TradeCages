package com.cosmocraft.trading_cells.feature.incubators.domain.model;

import com.cosmocraft.trading_cells.feature.machines.application.MachineSettings;

public final class IncubationCycle {
    private IncubationCycle() {
    }

    public static int ticksToAdult(IncubatorKind kind) {
        return kind == IncubatorKind.VILLAGER
                ? MachineSettings.values().villagerIncubatorTicks()
                : MachineSettings.values().piglinIncubatorTicks();
    }

    public static int advance(IncubatorKind kind, int currentTicks) {
        return Math.min(ticksToAdult(kind), Math.max(0, currentTicks) + 1);
    }

    public static boolean isComplete(IncubatorKind kind, int ticks) {
        return ticks >= ticksToAdult(kind);
    }
}
