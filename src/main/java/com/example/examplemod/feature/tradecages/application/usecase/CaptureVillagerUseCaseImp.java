package com.example.examplemod.feature.tradecages.application.usecase;
import com.example.examplemod.feature.tradecages.application.port.input.CaptureVillagerUseCase;
import com.example.examplemod.feature.tradecages.domain.model.TradingCellConfig;
import com.example.examplemod.feature.tradecages.domain.model.TradeVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import java.util.ArrayList;
import java.util.List;
public class CaptureVillagerUseCaseImp implements CaptureVillagerUseCase {
    private final TradingCellConfig config;
    private final List<TradeVillager> capturedVillagers;
    public CaptureVillagerUseCaseImp(TradingCellConfig config) {
        this.config = config;
        this.capturedVillagers = new ArrayList<>();
    }
    @Override
    public boolean captureVillager(Villager villager) {
        if (!hasCapacity()) {
            return false;
        }
        // Get villager type from the entity class name
        String villagerType = villager.getClass().getSimpleName();
        // Get experience - use 0 as default for now
        int experience = 0;

        TradeVillager tradeVillager = new TradeVillager(
                villager.getUUID(),
                villagerType,
                experience,
                System.currentTimeMillis()
        );
        capturedVillagers.add(tradeVillager);
        return true;
    }
    @Override
    public boolean hasCapacity() {
        return capturedVillagers.size() < config.maxVillagers();
    }
    @Override
    public int getVillagerCount() {
        return capturedVillagers.size();
    }
    public List<TradeVillager> getCapturedVillagers() {
        return new ArrayList<>(capturedVillagers);
    }
    public void removeVillager(int index) {
        if (index >= 0 && index < capturedVillagers.size()) {
            capturedVillagers.remove(index);
        }
    }
}
