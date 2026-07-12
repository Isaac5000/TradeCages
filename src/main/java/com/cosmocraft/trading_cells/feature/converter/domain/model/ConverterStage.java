package com.cosmocraft.trading_cells.feature.converter.domain.model;

import com.cosmocraft.trading_cells.feature.machines.application.MachineSettings;

public enum ConverterStage {
    IDLE,
    INFECTING,
    CURING;

    public int durationTicks() {
        return switch (this) {
            case IDLE -> 0;
            case INFECTING -> MachineSettings.values().converterInfectionTicks();
            case CURING -> MachineSettings.values().converterCureTicks();
        };
    }

    public boolean isProcessing() {
        return this != IDLE;
    }

    public static ConverterStage fromId(int id) {
        ConverterStage[] values = values();
        return values[Math.max(0, Math.min(values.length - 1, id))];
    }
}
