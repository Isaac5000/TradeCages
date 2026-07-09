package com.example.examplemod.feature.tradecages.adapters.input;

import com.mojang.serialization.DynamicOps;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class VillagerTradingCellBlockItem extends BlockItem {
    private static final String LEGACY_VILLAGER_DATA_TAG = "StoredVillager";
    private static final String ENTITY_KIND_TAG = "StoredEntityKind";
    private static final String ENTITY_DATA_TAG = "StoredEntity";
    private static final String POI_STACK_TAG = "StoredPoi";
    private static final String VILLAGER_KIND = "villager";
    private static final String VILLAGER_DATA_TAG = "VillagerData";
    private static final String PROFESSION_TAG = "profession";
    private static final String LEVEL_TAG = "level";
    private static final String XP_TAG = "Xp";
    private static final String AGE_TAG = "Age";
    private static final String NONE_PROFESSION = "minecraft:none";

    public VillagerTradingCellBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(
            @NonNull ItemStack itemStack,
            Item.@NonNull TooltipContext context,
            @NonNull TooltipDisplay display,
            @NonNull Consumer<Component> builder,
            @NonNull TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);

        CompoundTag blockEntityTag = getBlockEntityTag(itemStack);
        if (blockEntityTag == null) {
            return;
        }

        CompoundTag villagerData = getVillagerData(blockEntityTag);
        ItemStack poiStack = getPoiStack(blockEntityTag, context.registries());
        if (villagerData == null && poiStack.isEmpty()) {
            return;
        }

        builder.accept(Component.translatable("tooltip.trading_cells.employment", getEmploymentName(villagerData)).withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.trading_cells.employment_level", getEmploymentLevel(villagerData)).withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable(
                "tooltip.trading_cells.poi",
                poiStack.isEmpty() ? Component.translatable("tooltip.trading_cells.none") : poiStack.getHoverName()
        ).withStyle(ChatFormatting.GRAY));
    }

    private static @Nullable CompoundTag getBlockEntityTag(ItemStack stack) {
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        return data == null ? null : data.copyTagWithoutId();
    }

    private static @Nullable CompoundTag getVillagerData(CompoundTag blockEntityTag) {
        String kind = blockEntityTag.getString(ENTITY_KIND_TAG).orElse("");
        if (VILLAGER_KIND.equals(kind)) {
            return blockEntityTag.getCompound(ENTITY_DATA_TAG).map(CompoundTag::copy).orElse(null);
        }
        return blockEntityTag.getCompound(LEGACY_VILLAGER_DATA_TAG).map(CompoundTag::copy).orElse(null);
    }

    private static ItemStack getPoiStack(CompoundTag blockEntityTag, HolderLookup.@Nullable Provider registries) {
        CompoundTag poiTag = blockEntityTag.getCompound(POI_STACK_TAG).orElse(null);
        if (poiTag == null || poiTag.isEmpty() || registries == null) {
            return ItemStack.EMPTY;
        }

        DynamicOps<net.minecraft.nbt.Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.OPTIONAL_CODEC.parse(ops, poiTag).result().orElse(ItemStack.EMPTY);
    }

    private static Component getEmploymentName(@Nullable CompoundTag villagerData) {
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

        String professionName = professionId.contains(":")
                ? professionId.substring(professionId.indexOf(':') + 1)
                : professionId;
        return Component.translatable("entity.minecraft.villager." + professionName);
    }

    private static int getEmploymentLevel(@Nullable CompoundTag villagerData) {
        if (villagerData == null || villagerData.getInt(XP_TAG).orElse(0) == 0) {
            return 0;
        }

        return villagerData.getCompound(VILLAGER_DATA_TAG)
                .flatMap(data -> data.getInt(LEVEL_TAG))
                .orElse(0);
    }
}
