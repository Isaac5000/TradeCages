package com.example.examplemod.feature.breeders.domain.model;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BreederRecipe {
    public static final int VILLAGER_BREED_TICKS = 50;
    public static final int PIGLIN_BREED_TICKS = 50;

    private BreederRecipe() {
    }

    public static boolean isFood(BreederKind kind, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return switch (kind) {
            case VILLAGER -> stack.is(Items.BREAD) || stack.is(Items.CARROT) || stack.is(Items.POTATO) || stack.is(Items.BEETROOT);
            case PIGLIN -> stack.is(Items.PORKCHOP) || stack.is(Items.COOKED_PORKCHOP);
        };
    }

    public static int cost(BreederKind kind, ItemStack stack) {
        if (!isFood(kind, stack)) {
            return Integer.MAX_VALUE;
        }
        return switch (kind) {
            case VILLAGER -> stack.is(Items.BREAD) ? 3 : 12;
            case PIGLIN -> 4;
        };
    }

    public static int breedTicks(BreederKind kind) {
        return kind == BreederKind.VILLAGER ? VILLAGER_BREED_TICKS : PIGLIN_BREED_TICKS;
    }
}
