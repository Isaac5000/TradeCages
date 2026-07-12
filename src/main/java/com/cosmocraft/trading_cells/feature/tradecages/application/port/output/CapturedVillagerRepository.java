package com.cosmocraft.trading_cells.feature.tradecages.application.port.output;

import com.cosmocraft.trading_cells.feature.tradecages.domain.model.TradeVillager;

import java.util.List;

public interface CapturedVillagerRepository {
    boolean add(TradeVillager villager);

    boolean remove(int index);

    int count();

    List<TradeVillager> findAll();
}
