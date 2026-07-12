package com.cosmocraft.trading_cells.feature.ironfarm.domain.model;

import com.cosmocraft.trading_cells.feature.machines.application.MachineSettings;

public final class IronFarmCycle {
    private IronFarmCycle() {
    }

    public static int cycleTicks() {
        return MachineSettings.values().ironFarmCycleTicks();
    }

    public static int multiplier(int villagerCount) {
        return switch (Math.max(0, Math.min(3, villagerCount))) {
            case 1 -> MachineSettings.values().ironFarmOneVillagerMultiplier();
            case 2 -> MachineSettings.values().ironFarmTwoVillagerMultiplier();
            case 3 -> MachineSettings.values().ironFarmThreeVillagerMultiplier();
            default -> 0;
        };
    }

    public static boolean isGolemVisible(int cycleTicks) {
        return cycleTicks >= cycleTicks() - golemAttackTicks();
    }

    public static boolean isGolemHitTick(int cycleTicks) {
        if (!isGolemVisible(cycleTicks) || cycleTicks >= cycleTicks()) {
            return false;
        }
        return (cycleTicks - (cycleTicks() - golemAttackTicks())) % golemHitInterval() == 0;
    }

    public static boolean hasRedHitFlash(int cycleTicks) {
        if (!isGolemVisible(cycleTicks)) {
            return false;
        }
        int elapsed = cycleTicks - (cycleTicks() - golemAttackTicks());
        return elapsed % golemHitInterval() < golemRedFlashTicks();
    }

    public static boolean isRedHitFlashEnding(int cycleTicks) {
        if (!isGolemVisible(cycleTicks)) {
            return false;
        }
        int elapsed = cycleTicks - (cycleTicks() - golemAttackTicks());
        return elapsed % golemHitInterval() == golemRedFlashTicks();
    }

    private static int golemAttackTicks() {
        return Math.min(cycleTicks(), MachineSettings.values().ironGolemAttackTicks());
    }

    private static int golemHitInterval() {
        return Math.max(1, MachineSettings.values().ironGolemHitInterval());
    }

    private static int golemRedFlashTicks() {
        return Math.min(golemHitInterval() - 1, MachineSettings.values().ironGolemRedFlashTicks());
    }
}
