package com.example.examplemod.feature.tradecages.adapters.output;
import com.example.examplemod.feature.tradecages.adapters.input.TradeCageBlock;
import com.example.examplemod.feature.tradecages.application.usecase.CaptureVillagerUseCaseImp;
import com.example.examplemod.feature.tradecages.application.usecase.ReleaseVillagerUseCaseImp;
import com.example.examplemod.feature.tradecages.domain.model.TradeCageConfig;
import com.example.examplemod.platform.neoforge.bootstrap.ExampleMod;
import com.example.examplemod.platform.neoforge.registration.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
@SuppressWarnings("java:S1118")
public class TradeCageRegistrationAdapter {
    private static final TradeCageConfig TRADE_CAGE_CONFIG = new TradeCageConfig(10, 0, 8, true);
    public static final CaptureVillagerUseCaseImp CAPTURE_VILLAGER_USE_CASE =
            new CaptureVillagerUseCaseImp(TRADE_CAGE_CONFIG);
    @SuppressWarnings("unused")
    private static final ReleaseVillagerUseCaseImp RELEASE_VILLAGER_USE_CASE =
            new ReleaseVillagerUseCaseImp(CAPTURE_VILLAGER_USE_CASE);
    public static final DeferredBlock<TradeCageBlock> TRADE_CAGE_BLOCK =
            Registration.BLOCKS.register("trade_cage", () ->
                    new TradeCageBlock(BlockBehaviour.Properties.of().setId(
                            ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ExampleMod.MOD_ID, "trade_cage"))
                    ))
            );
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TRADE_CAGE_ITEM =
            Registration.ITEMS.registerSimpleBlockItem("trade_cage", TRADE_CAGE_BLOCK);
    public static final DeferredItem<Item> CAPTURED_VILLAGER_ITEM =
            Registration.ITEMS.registerSimpleItem("captured_villager", properties -> properties.stacksTo(16));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TRADE_CAGE_TAB =
            Registration.CREATIVE_MODE_TABS.register("trade_cage_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ExampleMod.MOD_ID))
                    .icon(() -> TRADE_CAGE_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(TRADE_CAGE_ITEM.get());
                        output.accept(CAPTURED_VILLAGER_ITEM.get());
                    })
                    .build());
    public static void load() {
        // No logic needed, just forcing class loading
    }
}
