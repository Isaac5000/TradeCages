package com.example.examplemod.feature.tradecages.application.usecase;

import com.example.examplemod.feature.tradecages.application.port.input.ReleaseVillagerUseCase;

public class ReleaseVillagerUseCaseImp implements ReleaseVillagerUseCase {

    private final CaptureVillagerUseCaseImp captureUseCase;

    public ReleaseVillagerUseCaseImp(CaptureVillagerUseCaseImp captureUseCase) {
        this.captureUseCase = captureUseCase;
    }

    @Override
    public void releaseVillager(int index) {
        captureUseCase.removeVillager(index);
    }

    @Override
    public int getStoredVillagerCount() {
        return captureUseCase.getVillagerCount();
    }
}

