package com.example.examplemod.platform.neoforge.bootstrap;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// Only client side
@Mod(value = ExampleMod.MOD_ID, dist = Dist.CLIENT)
public class ExampleModClient {

    @SuppressWarnings("java:S1118") // Fake warning
    public ExampleModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}