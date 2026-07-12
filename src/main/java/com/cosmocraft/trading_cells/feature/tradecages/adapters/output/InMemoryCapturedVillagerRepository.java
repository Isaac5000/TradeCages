package com.cosmocraft.trading_cells.feature.tradecages.adapters.output;

import com.cosmocraft.trading_cells.feature.tradecages.application.port.output.CapturedVillagerRepository;
import com.cosmocraft.trading_cells.feature.tradecages.domain.model.TradeVillager;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryCapturedVillagerRepository implements CapturedVillagerRepository {
    private final List<TradeVillager> villagers = new ArrayList<>();

    @Override
    public boolean add(TradeVillager villager) {
        return villagers.add(villager);
    }

    @Override
    public boolean remove(int index) {
        if (index < 0 || index >= villagers.size()) {
            return false;
        }
        villagers.remove(index);
        return true;
    }

    @Override
    public int count() {
        return villagers.size();
    }

    @Override
    public List<TradeVillager> findAll() {
        return List.copyOf(villagers);
    }
}
