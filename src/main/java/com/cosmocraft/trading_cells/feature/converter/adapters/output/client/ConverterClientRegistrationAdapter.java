package com.cosmocraft.trading_cells.feature.converter.adapters.output.client;

import com.cosmocraft.trading_cells.feature.converter.adapters.output.ConverterRegistrationAdapter;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ConverterClientRegistrationAdapter {
    private ConverterClientRegistrationAdapter() {
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ConverterRegistrationAdapter.CONVERTER_MENU.get(), ConverterScreen::new);
    }

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ConverterRegistrationAdapter.CONVERTER_BLOCK_ENTITY.get(), ConverterBlockEntityRenderer::new);
    }
}
