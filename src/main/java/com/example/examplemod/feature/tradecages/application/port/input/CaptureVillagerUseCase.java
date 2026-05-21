package com.example.examplemod.feature.tradecages.application.port.input;

import net.minecraft.world.entity.npc.villager.Villager;

public interface CaptureVillagerUseCase {
    boolean hasCapacity();

    boolean captureVillager(Villager villager);

    int getVillagerCount();
}
