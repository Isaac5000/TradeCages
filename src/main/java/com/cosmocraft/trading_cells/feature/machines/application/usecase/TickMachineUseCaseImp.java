package com.cosmocraft.trading_cells.feature.machines.application.usecase;

import com.cosmocraft.trading_cells.feature.machines.application.port.input.TickMachineUseCase;
import com.cosmocraft.trading_cells.feature.machines.application.port.output.MachineTickPort;

public final class TickMachineUseCaseImp implements TickMachineUseCase {
    @Override
    public void tick(MachineTickPort machine) {
        machine.processTick();
    }
}
