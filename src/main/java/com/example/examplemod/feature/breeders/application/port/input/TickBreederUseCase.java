package com.example.examplemod.feature.breeders.application.port.input;

import com.example.examplemod.feature.breeders.adapters.input.BreederBlockEntity;

public interface TickBreederUseCase {
    void tick(BreederBlockEntity breeder);
}
