package com.cosmocraft.trading_cells.feature.incubators.adapters.output.client;

import com.cosmocraft.trading_cells.feature.incubators.adapters.output.IncubatorRegistrationAdapter;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class IncubatorClientRegistrationAdapter {
    private IncubatorClientRegistrationAdapter() {
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(IncubatorRegistrationAdapter.VILLAGER_INCUBATOR_MENU.get(), IncubatorScreen::new);
        event.register(IncubatorRegistrationAdapter.PIGLIN_INCUBATOR_MENU.get(), IncubatorScreen::new);
    }

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                IncubatorRegistrationAdapter.VILLAGER_INCUBATOR_BLOCK_ENTITY.get(),
                IncubatorBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                IncubatorRegistrationAdapter.PIGLIN_INCUBATOR_BLOCK_ENTITY.get(),
                IncubatorBlockEntityRenderer::new
        );
    }
}
