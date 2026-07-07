package com.example.examplemod.platform.neoforge.registration;

import com.example.examplemod.feature.tradecages.adapters.output.TradingCellsRegistrationAdapter;
import com.example.examplemod.platform.neoforge.bootstrap.TradingCells;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Registration {

    private Registration() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TradingCells.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TradingCells.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TradingCells.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TradingCells.MOD_ID);

    public static void init(IEventBus modEventBus) {
        loadFeatures();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    private static void loadFeatures() {
        TradingCellsRegistrationAdapter.load();
    }
}
