package com.cosmocraft.trading_cells.feature.incubators.application.usecase;

import com.cosmocraft.trading_cells.feature.incubators.application.port.input.TickIncubatorUseCase;
import com.cosmocraft.trading_cells.feature.incubators.application.port.output.IncubatorTickPort;

public final class TickIncubatorUseCaseImp implements TickIncubatorUseCase {
    @Override
    public void tick(IncubatorTickPort incubator) {
        incubator.processIncubation();
    }
}
