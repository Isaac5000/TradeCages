package com.cosmocraft.trading_cells.feature.breeders.application.usecase;

import com.cosmocraft.trading_cells.feature.breeders.application.port.input.TickBreederUseCase;
import com.cosmocraft.trading_cells.feature.breeders.application.port.output.BreederTickPort;

public final class TickBreederUseCaseImp implements TickBreederUseCase {
    @Override
    public void tick(BreederTickPort breeder) {
        breeder.processAutomation();
        breeder.processBreedingProgress();
    }
}
