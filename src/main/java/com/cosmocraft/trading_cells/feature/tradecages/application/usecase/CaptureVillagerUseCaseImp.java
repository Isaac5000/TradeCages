package com.cosmocraft.trading_cells.feature.tradecages.application.usecase;

import com.cosmocraft.trading_cells.feature.tradecages.application.port.input.CaptureVillagerUseCase;
import com.cosmocraft.trading_cells.feature.tradecages.application.port.output.CapturedVillagerRepository;
import com.cosmocraft.trading_cells.feature.tradecages.domain.model.TradeVillager;
import com.cosmocraft.trading_cells.feature.tradecages.domain.model.TradingCellConfig;

public class CaptureVillagerUseCaseImp implements CaptureVillagerUseCase {
    private final TradingCellConfig config;
    private final CapturedVillagerRepository repository;

    public CaptureVillagerUseCaseImp(TradingCellConfig config, CapturedVillagerRepository repository) {
        this.config = config;
        this.repository = repository;
    }

    @Override
    public boolean captureVillager(TradeVillager villager) {
        return hasCapacity() && repository.add(villager);
    }

    @Override
    public boolean hasCapacity() {
        return repository.count() < config.maxVillagers();
    }

    @Override
    public int getVillagerCount() {
        return repository.count();
    }
}
