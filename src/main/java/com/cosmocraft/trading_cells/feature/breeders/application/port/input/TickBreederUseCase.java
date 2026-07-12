package com.cosmocraft.trading_cells.feature.breeders.application.port.input;

import com.cosmocraft.trading_cells.feature.breeders.application.port.output.BreederTickPort;

public interface TickBreederUseCase {
    void tick(BreederTickPort breeder);
}
