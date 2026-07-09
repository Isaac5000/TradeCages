package com.example.examplemod.feature.breeders.application.usecase;

import com.example.examplemod.feature.breeders.adapters.input.BreederBlockEntity;
import com.example.examplemod.feature.breeders.application.port.input.TickBreederUseCase;

public final class TickBreederUseCaseImp implements TickBreederUseCase {
    @Override
    public void tick(BreederBlockEntity breeder) {
        breeder.processAutomation();
        breeder.processBreedingProgress();
    }
}
