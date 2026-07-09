package com.example.examplemod.feature.breeders.adapters.output.client;

import com.example.examplemod.feature.breeders.adapters.output.BreederRegistrationAdapter;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class BreederClientRegistrationAdapter {
    private BreederClientRegistrationAdapter() {
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(BreederRegistrationAdapter.VILLAGER_BREEDER_MENU.get(), BreederScreen::new);
        event.register(BreederRegistrationAdapter.PIGLIN_BREEDER_MENU.get(), BreederScreen::new);
    }
}
