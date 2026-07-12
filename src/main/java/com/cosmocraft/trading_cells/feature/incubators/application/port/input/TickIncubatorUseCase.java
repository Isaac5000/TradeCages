package com.cosmocraft.trading_cells.feature.incubators.application.port.input;

import com.cosmocraft.trading_cells.feature.incubators.application.port.output.IncubatorTickPort;

public interface TickIncubatorUseCase {
    void tick(IncubatorTickPort incubator);
}
