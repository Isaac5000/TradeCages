package com.example.examplemod.feature.tradecages.adapters.output;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerTradingCellBlock;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.input.PiglinCapturerItem;
import com.example.examplemod.platform.neoforge.bootstrap.TradingCells;
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
public class TradingCellsRegistrationAdapter {
    private static final String TRADE_CAGE_ID = "villager_trading_cell";
    public static final DeferredBlock<VillagerTradingCellBlock> TRADE_CAGE_BLOCK =
            Registration.BLOCKS.register(TRADE_CAGE_ID, () ->
                    new VillagerTradingCellBlock(BlockBehaviour.Properties.of().setId(
                            ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, TRADE_CAGE_ID))
                    ))
            );
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TRADE_CAGE_ITEM =
            Registration.ITEMS.registerSimpleBlockItem(TRADE_CAGE_ID, TRADE_CAGE_BLOCK);
    public static final DeferredItem<VillagerCapturerItem> VILLAGER_CAPTURER_ITEM =
            Registration.ITEMS.register("villager_capturer", () -> new VillagerCapturerItem(
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, "villager_capturer"))).stacksTo(1)
            ));
    public static final DeferredItem<PiglinCapturerItem> PIGLIN_CAPTURER_ITEM =
            Registration.ITEMS.register("piglin_capturer", () -> new PiglinCapturerItem(
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, "piglin_capturer"))).stacksTo(1)
            ));
    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TRADE_CAGE_TAB =
            Registration.CREATIVE_MODE_TABS.register("villager_trader_cage_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + TradingCells.MOD_ID))
                    .icon(() -> VILLAGER_CAPTURER_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(TRADE_CAGE_ITEM.get());
                        output.accept(VILLAGER_CAPTURER_ITEM.get());
                        output.accept(PIGLIN_CAPTURER_ITEM.get());
                    })
                    .build());
    public static void load() {
        // No logic needed, just forcing class loading
    }
}
