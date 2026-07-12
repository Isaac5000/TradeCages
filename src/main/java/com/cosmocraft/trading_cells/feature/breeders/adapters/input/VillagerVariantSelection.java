package com.cosmocraft.trading_cells.feature.breeders.adapters.input;

import java.util.List;

public final class VillagerVariantSelection {
    private static final List<String> VARIANT_IDS = List.of(
            "minecraft:plains",
            "minecraft:desert",
            "minecraft:jungle",
            "minecraft:savanna",
            "minecraft:snow",
            "minecraft:swamp",
            "minecraft:taiga"
    );

    private VillagerVariantSelection() {
    }

    public static int count() {
        return VARIANT_IDS.size();
    }

    public static int normalize(int index) {
        return Math.floorMod(index, count());
    }

    public static String id(int index) {
        return VARIANT_IDS.get(normalize(index));
    }

    public static String translationKey(int index) {
        String id = id(index);
        return "variant.trading_cells." + id.substring(id.indexOf(':') + 1);
    }
}
