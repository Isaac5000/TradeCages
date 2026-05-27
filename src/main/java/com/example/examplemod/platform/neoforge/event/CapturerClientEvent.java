package com.example.examplemod.platform.neoforge.event;

import com.example.examplemod.feature.tradecages.adapters.output.client.PiglinCapturerItemRenderSupport;
import com.example.examplemod.feature.tradecages.adapters.output.client.VillagerCapturerItemRenderSupport;
import com.example.examplemod.platform.neoforge.bootstrap.TradingCells;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

public final class CapturerClientEvent {
    private CapturerClientEvent() {
    }

    // onRenderHand removed; rendering is handled by model-selected SpecialModelRenderers.

    public static void onRegisterSpecialModelRenderer(RegisterSpecialModelRendererEvent event) {
        register(event, "villager_capturer", VillagerCapturerItemRenderSupport.Default.Unbaked.MAP_CODEC);
        register(event, "villager_capturer_gui", VillagerCapturerItemRenderSupport.Gui.Unbaked.MAP_CODEC);
        register(event, "villager_capturer_fixed", VillagerCapturerItemRenderSupport.Fixed.Unbaked.MAP_CODEC);
        register(event, "villager_capturer_on_shelf", VillagerCapturerItemRenderSupport.OnShelf.Unbaked.MAP_CODEC);
        register(event, "villager_capturer_third_person", VillagerCapturerItemRenderSupport.ThirdPerson.Unbaked.MAP_CODEC);
        register(event, "villager_capturer_first_person", VillagerCapturerItemRenderSupport.FirstPerson.Unbaked.MAP_CODEC);
        // Piglin special renderers (reuse same per-profile pattern)
        register(event, "piglin_capturer", PiglinCapturerItemRenderSupport.Default.Unbaked.MAP_CODEC);
        register(event, "piglin_capturer_gui", PiglinCapturerItemRenderSupport.Gui.Unbaked.MAP_CODEC);
        register(event, "piglin_capturer_fixed", PiglinCapturerItemRenderSupport.Fixed.Unbaked.MAP_CODEC);
        register(event, "piglin_capturer_on_shelf", PiglinCapturerItemRenderSupport.OnShelf.Unbaked.MAP_CODEC);
        register(event, "piglin_capturer_third_person", PiglinCapturerItemRenderSupport.ThirdPerson.Unbaked.MAP_CODEC);
        register(event, "piglin_capturer_first_person", PiglinCapturerItemRenderSupport.FirstPerson.Unbaked.MAP_CODEC);
    }

    private static void register(RegisterSpecialModelRendererEvent event, String path, com.mojang.serialization.MapCodec<? extends net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked<?>> codec) {
        Identifier id = Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, path);
        TradingCells.LOGGER.debug("Registering special model renderer: {}", id);
        event.register(id, codec);
    }
}
