package com.example.examplemod.platform.neoforge.bootstrap;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// Only client side
@Mod(value = ExampleMod.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
public class ExampleModClient {

    @SuppressWarnings("java:S1118") // Fake warning
    public ExampleModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ExampleMod.LOGGER.info("HELLO FROM CLIENT SETUP");

        // FMLClientSetupEvent inherits from IModBusEvent, so NeoForge
        // automatically knows it should use the Mod Event Bus.
        event.enqueueWork(() -> {
            var user = Minecraft.getInstance().getUser();

            //Fake warning
            //noinspection ConstantValue
            if (user != null) {
                ExampleMod.LOGGER.info("MINECRAFT NAME >> {}", user.getName());
            }
        });
    }
}