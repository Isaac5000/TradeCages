package com.example.examplemod.feature.tradecages.adapters.input;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import com.example.examplemod.feature.tradecages.adapters.output.client.VillagerCapturerItemRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class VillagerCapturerItem extends Item {
    public VillagerCapturerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final VillagerCapturerItemRenderer renderer = new VillagerCapturerItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }

    public static boolean hasCapturedVillager(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null && !customData.isEmpty();
    }

    public static boolean captureVillager(ItemStack stack, Villager villager) {
        if (hasCapturedVillager(stack)) {
            return false;
        }

        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        villager.saveWithoutId(output);
        CompoundTag villagerData = output.buildResult();
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(villagerData));
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (!hasCapturedVillager(stack)) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Villager villager = releaseVillager(level, stack, context.getClickedPos().relative(context.getClickedFace()));
        if (villager == null || !level.addFreshEntity(villager)) {
            return InteractionResult.FAIL;
        }

        clearCapturedVillager(stack);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasCapturedVillager(stack) || super.isFoil(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (!hasCapturedVillager(stack)) {
            return super.getName(stack);
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return Component.translatable("entity.minecraft.villager.none");
        }

        CompoundTag tag = customData.copyTag();

        // First check if the villager has a custom name
        if (tag.contains("CustomName")) {
            var customNameOpt = tag.getString("CustomName");
            if (customNameOpt.isPresent()) {
                String customName = customNameOpt.get();
                if (!customName.isEmpty()) {
                    // Check if it's a baby villager
                    if (isBabyVillager(tag)) {
                        return Component.translatable("villager.baby", Component.literal(customName));
                    }
                    return Component.literal(customName);
                }
            }
        }

        // If no custom name, use translation based on profession
        String villagerKey = getVillagerKeyFromTag(tag);
        Component baseComponent = Component.translatable(villagerKey);

        // Check if it's a baby villager
        if (isBabyVillager(tag)) {
            return Component.translatable("villager.baby", baseComponent);
        }

        return baseComponent;
    }

    private static boolean isBabyVillager(CompoundTag tag) {
        // Check if the villager is a baby
        if (tag.contains("Age")) {
            var ageOpt = tag.getInt("Age");
            if (ageOpt.isPresent() && ageOpt.get() < 0) {
                return true;
            }
        }
        return false;
    }


    private static String getVillagerKeyFromTag(CompoundTag tag) {
        // Check if the villager has profession data
        if (tag.contains("VillagerData")) {
            var villagerDataOpt = tag.getCompound("VillagerData");
            if (villagerDataOpt.isPresent()) {
                CompoundTag villagerData = villagerDataOpt.get();
                if (villagerData.contains("profession")) {
                    var professionIdOpt = villagerData.getString("profession");
                    if (professionIdOpt.isPresent()) {
                        String professionId = professionIdOpt.get();

                        // Minecraft already provides these keys automatically
                        String professionName = professionId.contains(":")
                            ? professionId.substring(professionId.indexOf(":") + 1)
                            : professionId;

                        return "entity.minecraft.villager." + professionName;
                    }
                }
            }
        }

        // If no VillagerData, use dynamic Minecraft translation
        return "entity.minecraft.villager.none";
    }

    private static Villager releaseVillager(Level level, ItemStack stack, BlockPos position) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return null;
        }

        CompoundTag villagerData = customData.copyTag();
        Entity entity = EntityType.loadEntityRecursive(EntityType.VILLAGER, villagerData, level, EntitySpawnReason.LOAD, loadedEntity -> loadedEntity);
        if (!(entity instanceof Villager villager)) {
            return null;
        }

        villager.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
        villager.setPersistenceRequired();
        return villager;
    }

    private static void clearCapturedVillager(ItemStack stack) {
        stack.remove(DataComponents.CUSTOM_DATA);
    }

    public static Villager createCapturedVillager(Level level, ItemStack stack, BlockPos position) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return null;
        }

        CompoundTag villagerData = customData.copyTag();
        Entity entity = EntityType.loadEntityRecursive(EntityType.VILLAGER, villagerData, level, EntitySpawnReason.LOAD, loadedEntity -> loadedEntity);
        if (!(entity instanceof Villager villager)) {
            return null;
        }

        villager.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
        villager.setPersistenceRequired();
        return villager;
    }
}
