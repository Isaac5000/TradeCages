package com.example.examplemod.feature.tradecages.adapters.output;

import com.example.examplemod.feature.tradecages.adapters.input.PiglinBarteringCellBlock;
import com.example.examplemod.feature.tradecages.adapters.input.PiglinBarteringCellBlockEntity;
import com.example.examplemod.feature.tradecages.adapters.input.PiglinBarteringCellBlockItem;
import com.example.examplemod.feature.tradecages.adapters.input.PiglinCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerTradingCellBlock;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerTradingCellBlockEntity;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerTradingCellBlockItem;
import com.example.examplemod.platform.neoforge.bootstrap.TradingCells;
import com.example.examplemod.platform.neoforge.registration.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

@SuppressWarnings("java:S1118")
public class TradingCellsRegistrationAdapter {
    private static final String TRADE_CAGE_ID = "villager_trading_cell";
    private static final String PIGLIN_BARTERING_CELL_ID = "piglin_bartering_cell";

    public static final DeferredBlock<VillagerTradingCellBlock> TRADE_CAGE_BLOCK =
            Registration.BLOCKS.register(TRADE_CAGE_ID, () ->
                    new VillagerTradingCellBlock(BlockBehaviour.Properties.of().setId(
                            ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, TRADE_CAGE_ID))
                    ).strength(2.0F, 6.0F).noOcclusion()
                            .isRedstoneConductor((state, getter, pos) -> false)
                            .isSuffocating((state, getter, pos) -> false)
                            .isViewBlocking((state, getter, pos) -> false))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VillagerTradingCellBlockEntity>> VILLAGER_TRADING_CELL_BLOCK_ENTITY =
            Registration.BLOCK_ENTITY_TYPES.register(TRADE_CAGE_ID, () ->
                    new BlockEntityType<>(VillagerTradingCellBlockEntity::new, TRADE_CAGE_BLOCK.get())
            );

    public static final DeferredBlock<PiglinBarteringCellBlock> PIGLIN_BARTERING_CELL_BLOCK =
            Registration.BLOCKS.register(PIGLIN_BARTERING_CELL_ID, () ->
                    new PiglinBarteringCellBlock(BlockBehaviour.Properties.of().setId(
                            ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, PIGLIN_BARTERING_CELL_ID))
                    ).strength(2.0F, 6.0F).noOcclusion()
                            .isRedstoneConductor((state, getter, pos) -> false)
                            .isSuffocating((state, getter, pos) -> false)
                            .isViewBlocking((state, getter, pos) -> false))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PiglinBarteringCellBlockEntity>> PIGLIN_BARTERING_CELL_BLOCK_ENTITY =
            Registration.BLOCK_ENTITY_TYPES.register(PIGLIN_BARTERING_CELL_ID, () ->
                    new BlockEntityType<>(PiglinBarteringCellBlockEntity::new, PIGLIN_BARTERING_CELL_BLOCK.get())
            );

    public static final DeferredItem<VillagerTradingCellBlockItem> TRADE_CAGE_ITEM =
            Registration.ITEMS.register(TRADE_CAGE_ID, () -> new VillagerTradingCellBlockItem(
                    TRADE_CAGE_BLOCK.get(),
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, TRADE_CAGE_ID)))
            ));

    public static final DeferredItem<PiglinBarteringCellBlockItem> PIGLIN_BARTERING_CELL_ITEM =
            Registration.ITEMS.register(PIGLIN_BARTERING_CELL_ID, () -> new PiglinBarteringCellBlockItem(
                    PIGLIN_BARTERING_CELL_BLOCK.get(),
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, PIGLIN_BARTERING_CELL_ID)))
            ));

    public static final DeferredItem<VillagerCapturerItem> VILLAGER_CAPTURER_ITEM =
            Registration.ITEMS.register("villager_capturer", () -> new VillagerCapturerItem(
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, "villager_capturer")))
                            .stacksTo(64)
            ));

    public static final DeferredItem<PiglinCapturerItem> PIGLIN_CAPTURER_ITEM =
            Registration.ITEMS.register("piglin_capturer", () -> new PiglinCapturerItem(
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, "piglin_capturer")))
                            .stacksTo(64)
            ));

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TRADE_CAGE_TAB =
            Registration.CREATIVE_MODE_TABS.register("villager_trader_cage_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + TradingCells.MOD_ID))
                    .icon(() -> VILLAGER_CAPTURER_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(TRADE_CAGE_ITEM.get());
                        output.accept(PIGLIN_BARTERING_CELL_ITEM.get());
                        output.accept(VILLAGER_CAPTURER_ITEM.get());
                        output.accept(PIGLIN_CAPTURER_ITEM.get());
                    })
                    .build());

    public static void load() {
        // Forces class loading so all DeferredRegister entries are created.
    }
}
