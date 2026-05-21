package com.example.examplemod.platform.neoforge.bootstrap;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.ModConfigSpec;

@SuppressWarnings("java:S1118")
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common bootstrap")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER        //NOSONAR
            .comment("A list of items to log on common bootstrap.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    public static final ModConfigSpec SPEC = BUILDER.build();

    /**
     * Validates the item name by comparing the String directly with the
     * registered IDs in the game, avoiding the use of complex mapping classes.
     */
    private static boolean validateItemName(final Object obj) {
        if (obj instanceof String itemName) {
            // Search the registry to see if any item ID matches the string
            return BuiltInRegistries.ITEM.keySet().stream()
                    .anyMatch(id -> id.toString().equals(itemName));
        }
        return false;
    }
}