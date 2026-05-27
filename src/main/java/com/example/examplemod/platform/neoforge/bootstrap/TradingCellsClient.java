package com.example.examplemod.platform.neoforge.bootstrap;

import com.example.examplemod.platform.neoforge.event.CapturerClientEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
// NeoForge event bus listener for RenderHand removed: we rely on model-driven special renderers now.

// Only client side
@Mod(value = TradingCells.MOD_ID, dist = Dist.CLIENT)
public class TradingCellsClient {

    @SuppressWarnings("java:S1118") // Fake warning
    public TradingCellsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        var modBus = container.getEventBus();
        if (modBus != null) {
            modBus.addListener(CapturerClientEvent::onRegisterSpecialModelRenderer);
        }
        // RenderHand handling removed — rendering is handled by SpecialModelRenderers selected by JSON.
    }
}