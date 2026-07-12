package com.cosmocraft.trading_cells.feature.tradecages.application.usecase;

import com.cosmocraft.trading_cells.feature.tradecages.application.port.input.ReleaseVillagerUseCase;
import com.cosmocraft.trading_cells.feature.tradecages.application.port.output.CapturedVillagerRepository;

public class ReleaseVillagerUseCaseImp implements ReleaseVillagerUseCase {

    private final CapturedVillagerRepository repository;

    public ReleaseVillagerUseCaseImp(CapturedVillagerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void releaseVillager(int index) {
        repository.remove(index);
    }

    @Override
    public int getStoredVillagerCount() {
        return repository.count();
    }
}
