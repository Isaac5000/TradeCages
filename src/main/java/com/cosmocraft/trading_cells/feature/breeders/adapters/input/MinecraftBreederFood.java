package com.cosmocraft.trading_cells.feature.breeders.adapters.input;

import com.cosmocraft.trading_cells.feature.breeders.domain.model.BreederFood;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class MinecraftBreederFood {
    private MinecraftBreederFood() {
    }

    static BreederFood from(ItemStack stack) {
        if (stack.is(Items.BREAD)) {
            return BreederFood.BREAD;
        }
        if (stack.is(Items.CARROT) || stack.is(Items.POTATO) || stack.is(Items.BEETROOT)) {
            return BreederFood.VEGETABLE;
        }
        if (stack.is(Items.PORKCHOP) || stack.is(Items.COOKED_PORKCHOP)) {
            return BreederFood.PORK;
        }
        if (stack.is(Items.CRIMSON_FUNGUS)) {
            return BreederFood.CRIMSON_FUNGUS;
        }
        return BreederFood.NONE;
    }
}
