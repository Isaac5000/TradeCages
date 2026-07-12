package com.cosmocraft.trading_cells.feature.ironfarm.adapters.output.client;

import com.cosmocraft.trading_cells.feature.ironfarm.adapters.output.IronFarmRegistrationAdapter;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class IronFarmClientRegistrationAdapter {
    private IronFarmClientRegistrationAdapter() {
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(IronFarmRegistrationAdapter.IRON_FARM_MENU.get(), IronFarmScreen::new);
    }

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(IronFarmRegistrationAdapter.IRON_FARM_BLOCK_ENTITY.get(), IronFarmBlockEntityRenderer::new);
    }
}
