package com.cosmocraft.trading_cells.feature.machines.application.port.input;

import com.cosmocraft.trading_cells.feature.machines.application.port.output.MachineTickPort;

public interface TickMachineUseCase {
    void tick(MachineTickPort machine);
}
