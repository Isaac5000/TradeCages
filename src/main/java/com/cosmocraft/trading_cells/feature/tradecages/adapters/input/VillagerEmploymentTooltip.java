package com.cosmocraft.trading_cells.feature.tradecages.adapters.input;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class VillagerEmploymentTooltip {
    private static final String VILLAGER_DATA_TAG = "VillagerData";
    private static final String PROFESSION_TAG = "profession";
    private static final String LEVEL_TAG = "level";
    private static final String XP_TAG = "Xp";
    private static final String AGE_TAG = "Age";
    private static final String NONE_PROFESSION = "minecraft:none";

    private VillagerEmploymentTooltip() {
    }

    public static void append(
            @Nullable CompoundTag villagerData,
            ItemStack poiStack,
            HolderLookup.@Nullable Provider registries,
            Consumer<Component> tooltip
    ) {
        tooltip.accept(Component.translatable(
                "tooltip.trading_cells.employment",
                employmentName(villagerData, registries)
        ).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable(
                "tooltip.trading_cells.employment_level",
                employmentLevel(villagerData)
        ).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable(
                "tooltip.trading_cells.poi",
                poiStack.isEmpty() ? Component.translatable("tooltip.trading_cells.none") : poiStack.getHoverName()
        ).withStyle(ChatFormatting.GRAY));
    }

    private static Component employmentName(
            @Nullable CompoundTag villagerData,
            HolderLookup.@Nullable Provider registries
    ) {
        if (villagerData == null) {
            return Component.translatable("tooltip.trading_cells.unemployed");
        }
        if (villagerData.getInt(AGE_TAG).orElse(0) < 0) {
            return Component.translatable("tooltip.trading_cells.baby");
        }

        String professionId = villagerData.getCompound(VILLAGER_DATA_TAG)
                .flatMap(data -> data.getString(PROFESSION_TAG))
                .orElse(NONE_PROFESSION);
        if (professionId.isBlank() || NONE_PROFESSION.equals(professionId)) {
            return Component.translatable("tooltip.trading_cells.unemployed");
        }

        Identifier identifier = Identifier.tryParse(professionId);
        if (identifier == null) {
            return Component.literal(professionId);
        }
        if (registries != null) {
            ResourceKey<VillagerProfession> key = ResourceKey.create(Registries.VILLAGER_PROFESSION, identifier);
            Component registeredName = registries.lookup(Registries.VILLAGER_PROFESSION)
                    .flatMap(registry -> registry.get(key))
                    .map(holder -> holder.value().name())
                    .orElse(null);
            if (registeredName != null) {
                return registeredName;
            }
        }
        return Component.translatable(
                "entity." + identifier.getNamespace() + ".villager." + identifier.getPath()
        );
    }

    private static int employmentLevel(@Nullable CompoundTag villagerData) {
        if (villagerData == null || villagerData.getInt(XP_TAG).orElse(0) <= 0) {
            return 0;
        }
        return villagerData.getCompound(VILLAGER_DATA_TAG)
                .flatMap(data -> data.getInt(LEVEL_TAG))
                .orElse(0);
    }
}
