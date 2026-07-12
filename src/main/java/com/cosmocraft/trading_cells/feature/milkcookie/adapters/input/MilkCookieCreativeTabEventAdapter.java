package com.cosmocraft.trading_cells.feature.milkcookie.adapters.input;

import com.cosmocraft.trading_cells.feature.milkcookie.adapters.output.MilkCookieRegistrationAdapter;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public final class MilkCookieCreativeTabEventAdapter {
    private MilkCookieCreativeTabEventAdapter() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(MilkCookieCreativeTabEventAdapter::onBuildCreativeTabContents);
    }

    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (CreativeModeTabs.FOOD_AND_DRINKS.equals(event.getTabKey())) {
            event.accept(MilkCookieRegistrationAdapter.COOKIE_WITH_MILK_ITEM.get());
        }
    }
}
