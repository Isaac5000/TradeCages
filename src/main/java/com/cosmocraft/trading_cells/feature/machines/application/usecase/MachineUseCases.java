package com.cosmocraft.trading_cells.feature.machines.application.usecase;

import com.cosmocraft.trading_cells.feature.machines.application.port.input.TickMachineUseCase;

public final class MachineUseCases {
    public static final TickMachineUseCase TICK = new TickMachineUseCaseImp();

    private MachineUseCases() {
    }
}
