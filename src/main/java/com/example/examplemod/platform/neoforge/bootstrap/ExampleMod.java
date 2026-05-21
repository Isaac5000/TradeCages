package com.example.examplemod.platform.neoforge.bootstrap;

import com.example.examplemod.platform.neoforge.registration.Registration;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@SuppressWarnings("java:S1118") // Fake
@Mod(ExampleMod.MOD_ID)
public class ExampleMod {
    public static final String MOD_ID = "tradecages";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        // 1. Initialize Registries (Output Adapters to Minecraft)
        Registration.init(modEventBus);

        // 2. Register the configuration
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}