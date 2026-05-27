package com.example.examplemod.feature.tradecages.adapters.input;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class PiglinCapturerItem extends Item {
    public PiglinCapturerItem(Properties properties) {
        super(properties);
    }

    public static boolean hasCapturedPiglin(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null && !customData.isEmpty();
    }

    public static boolean capturePiglin(ItemStack stack, Piglin piglin) {
        if (hasCapturedPiglin(stack)) {
            return false;
        }

        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        piglin.saveWithoutId(output);
        CompoundTag piglinData = output.buildResult();
        // Remove transient data we do not want to persist: position/rotation/motion and lighting.
        // Also remove hurt/damage state so the stored entity doesn't keep the red hurt tint.
        piglinData.remove("Pos");
        piglinData.remove("Motion");
        piglinData.remove("Rotation");
        piglinData.remove("FallDistance");
        piglinData.remove("Air");
        piglinData.remove("OnGround");
        // Remove hurt-related tags that cause the red damage overlay
        piglinData.remove("HurtTime");
        piglinData.remove("HurtByTimestamp");
        piglinData.remove("LastHurtByPlayer");
        piglinData.remove("LastHurt");
        // Do not store any captured-light value here; render side decides lighting.
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(piglinData));
        return true;
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (!hasCapturedPiglin(stack)) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Piglin piglin = releasePiglin(level, stack, context.getClickedPos().relative(context.getClickedFace()));
        if (piglin == null || !level.addFreshEntity(piglin)) {
            return InteractionResult.FAIL;
        }

        clearCapturedPiglin(stack);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isFoil(@NonNull ItemStack stack) {
        return hasCapturedPiglin(stack) || super.isFoil(stack);
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack stack) {
        if (!hasCapturedPiglin(stack)) {
            return super.getName(stack);
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return Component.translatable("entity.minecraft.piglin");
        }

        CompoundTag tag = customData.copyTag();

        // First check if the piglin has a custom name
        if (tag.contains("CustomName")) {
            var customNameOpt = tag.getString("CustomName");
            if (customNameOpt.isPresent()) {
                String customName = customNameOpt.get();
                if (!customName.isEmpty()) {
                    // Check if it's a baby piglin
                    if (isBabyPiglin(tag)) {
                        return Component.translatable("piglin.baby", Component.literal(customName));
                    }
                    return Component.literal(customName);
                }
            }
        }

        // If no custom name, use base piglin translation
        Component baseComponent = Component.translatable("entity.minecraft.piglin");

        if (isBabyPiglin(tag)) {
            return Component.translatable("piglin.baby", baseComponent);
        }

        return baseComponent;
    }

    private static boolean isBabyPiglin(CompoundTag tag) {
        // Some entities (including piglins) may use an explicit IsBaby boolean, others use Age < 0.
        if (tag.contains("IsBaby")) {
            var isBabyOpt = tag.getBoolean("IsBaby");
            if (isBabyOpt.isPresent() && isBabyOpt.get()) {
                return true;
            }
        }

        if (tag.contains("Age")) {
            var ageOpt = tag.getInt("Age");
            if (ageOpt.isPresent() && ageOpt.get() < 0) {
                return true;
            }
        }

        return false;
    }

    private static Piglin releasePiglin(Level level, ItemStack stack, BlockPos position) {
        Piglin piglin = loadPiglinFromStack(level, stack);
        if (piglin == null) return null;
        piglin.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
        piglin.setPersistenceRequired();
        return piglin;
    }

    private static void clearCapturedPiglin(ItemStack stack) {
        stack.remove(DataComponents.CUSTOM_DATA);
    }

    public static Piglin createCapturedPiglin(Level level, ItemStack stack, BlockPos position) {
        Piglin piglin = loadPiglinFromStack(level, stack);
        if (piglin == null) return null;
        piglin.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
        piglin.setPersistenceRequired();
        return piglin;
    }

    // Helper to avoid duplicating entity-loading code in multiple methods
    private static Piglin loadPiglinFromStack(Level level, ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return null;
        }

        CompoundTag piglinData = customData.copyTag();
        Entity entity = EntityType.loadEntityRecursive(EntityType.PIGLIN, piglinData, level, EntitySpawnReason.LOAD, loadedEntity -> loadedEntity);
        if (!(entity instanceof Piglin piglin)) {
            return null;
        }
        return piglin;
    }
}
