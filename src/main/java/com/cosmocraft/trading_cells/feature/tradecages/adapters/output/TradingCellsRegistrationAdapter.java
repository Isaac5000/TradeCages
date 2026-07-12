package com.cosmocraft.trading_cells.feature.tradecages.adapters.output;

import com.cosmocraft.trading_cells.feature.autotrader.adapters.output.AutotraderRegistrationAdapter;
import com.cosmocraft.trading_cells.feature.breeders.adapters.output.BreederRegistrationAdapter;
import com.cosmocraft.trading_cells.feature.converter.adapters.output.ConverterRegistrationAdapter;
import com.cosmocraft.trading_cells.feature.farmer.adapters.output.FarmerRegistrationAdapter;
import com.cosmocraft.trading_cells.feature.incubators.adapters.output.IncubatorRegistrationAdapter;
import com.cosmocraft.trading_cells.feature.ironfarm.adapters.output.IronFarmRegistrationAdapter;

import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.PiglinBarteringCellBlock;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.PiglinBarteringCellBlockEntity;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.PiglinBarteringCellBlockItem;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.PiglinCapturerItem;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.VillagerTradingCellBlock;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.VillagerTradingCellBlockEntity;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.VillagerTradingCellBlockItem;
import com.cosmocraft.trading_cells.platform.neoforge.bootstrap.TradingCells;
import com.cosmocraft.trading_cells.platform.neoforge.machine.MachineBlockProperties;
import com.cosmocraft.trading_cells.platform.neoforge.registration.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

@SuppressWarnings("java:S1118")
public class TradingCellsRegistrationAdapter {
    private static final String TRADE_CAGE_ID = "villager_trading_cell";
    private static final String PIGLIN_BARTERING_CELL_ID = "piglin_bartering_cell";

    public static final DeferredBlock<VillagerTradingCellBlock> TRADE_CAGE_BLOCK =
            Registration.BLOCKS.register(TRADE_CAGE_ID, () ->
                    new VillagerTradingCellBlock(MachineBlockProperties.create(TRADE_CAGE_ID))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VillagerTradingCellBlockEntity>> VILLAGER_TRADING_CELL_BLOCK_ENTITY =
            Registration.BLOCK_ENTITY_TYPES.register(TRADE_CAGE_ID, () ->
                    new BlockEntityType<>(VillagerTradingCellBlockEntity::new, TRADE_CAGE_BLOCK.get())
            );

    public static final DeferredBlock<PiglinBarteringCellBlock> PIGLIN_BARTERING_CELL_BLOCK =
            Registration.BLOCKS.register(PIGLIN_BARTERING_CELL_ID, () ->
                    new PiglinBarteringCellBlock(MachineBlockProperties.create(PIGLIN_BARTERING_CELL_ID))
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
                        output.accept(BreederRegistrationAdapter.VILLAGER_BREEDER_ITEM.get());
                        output.accept(BreederRegistrationAdapter.PIGLIN_BREEDER_ITEM.get());
                        output.accept(IncubatorRegistrationAdapter.VILLAGER_INCUBATOR_ITEM.get());
                        output.accept(IncubatorRegistrationAdapter.PIGLIN_INCUBATOR_ITEM.get());
                        output.accept(FarmerRegistrationAdapter.FARMER_ITEM.get());
                        output.accept(AutotraderRegistrationAdapter.AUTOTRADER_ITEM.get());
                        output.accept(IronFarmRegistrationAdapter.IRON_FARM_ITEM.get());
                        output.accept(ConverterRegistrationAdapter.CONVERTER_ITEM.get());
                        output.accept(VILLAGER_CAPTURER_ITEM.get());
                        output.accept(PIGLIN_CAPTURER_ITEM.get());
                    })
                    .build());

    public static void load() {
        // Forces class loading so all DeferredRegister entries are created.
    }
}
