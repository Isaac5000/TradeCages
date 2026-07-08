package com.example.examplemod.feature.tradecages.adapters.input;

import com.example.examplemod.feature.tradecages.adapters.output.TradingCellsRegistrationAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class PiglinBarteringCellBlockEntity extends BlockEntity implements WorldlyContainer {
    private static final String PIGLIN_DATA_TAG = "StoredPiglin";
    private static final String GOLD_BUFFER_TAG = "GoldBuffer";
    private static final String OUTPUT_BUFFER_TAG = "OutputBuffer";
    private static final String BARTER_TICKS_TAG = "BarterTicksRemaining";
    private static final int GOLD_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int[] TOP_SLOTS = new int[]{GOLD_SLOT};
    private static final int[] BOTTOM_SLOTS = new int[]{OUTPUT_SLOT};
    private static final int[] NO_SLOTS = new int[0];
    private static final int BARTER_TIME_TICKS = 20;

    private @Nullable CompoundTag storedPiglinData;
    private ItemStack goldBuffer = ItemStack.EMPTY;
    private ItemStack outputBuffer = ItemStack.EMPTY;
    private int barterTicksRemaining = 0;

    public PiglinBarteringCellBlockEntity(BlockPos pos, BlockState blockState) {
        super(TradingCellsRegistrationAdapter.PIGLIN_BARTERING_CELL_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PiglinBarteringCellBlockEntity cell) {
        if (level instanceof ServerLevel serverLevel) {
            cell.tickBarter(serverLevel);
        }
    }

    public boolean hasPiglin() {
        return storedPiglinData != null && !storedPiglinData.isEmpty();
    }

    public boolean hasOutput() {
        return !outputBuffer.isEmpty();
    }

    public boolean isBartering() {
        return barterTicksRemaining > 0;
    }

    public @NonNull ItemStack copyOutputStack() {
        return outputBuffer.copy();
    }

    public @Nullable CompoundTag copyPiglinData() {
        return hasPiglin() && storedPiglinData != null ? storedPiglinData.copy() : null;
    }

    public @Nullable Piglin createPiglinForDisplay() {
        if (level == null || !hasPiglin() || storedPiglinData == null) {
            return null;
        }

        Piglin piglin = PiglinCapturerItem.createCapturedPiglin(level, storedPiglinData, worldPosition);
        if (piglin != null && isBartering()) {
            piglin.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.GOLD_INGOT));
        }
        return piglin;
    }

    public InteractionResult insertPiglinFromCapturer(ItemStack stack, Player player) {
        if (hasPiglin()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.cell_occupied"));
            return InteractionResult.SUCCESS_SERVER;
        }

        CompoundTag piglinData = PiglinCapturerItem.getCapturedPiglinData(stack);
        if (piglinData == null) {
            return InteractionResult.PASS;
        }

        storedPiglinData = piglinData.copy();
        PiglinCapturerItem.clearCapturedPiglin(stack);
        markChangedAndSync();
        player.sendSystemMessage(Component.translatable("message.trading_cells.piglin_inserted"));
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult extractPiglinToCapturer(ItemStack stack, Player player) {
        if (!hasPiglin()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.cell_empty"));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (PiglinCapturerItem.hasCapturedPiglin(stack)) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.capturer_occupied"));
            return InteractionResult.SUCCESS_SERVER;
        }

        CompoundTag piglinData = copyPiglinData();
        if (piglinData == null) {
            return InteractionResult.FAIL;
        }

        PiglinCapturerItem.setCapturedPiglinData(stack, piglinData);
        clearStoredPiglin();
        player.sendSystemMessage(Component.translatable("message.trading_cells.piglin_extracted"));
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult barterGold(ItemStack goldStack, Player player) {
        if (goldStack.isEmpty() || !goldStack.is(Items.GOLD_INGOT)) {
            return InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS_SERVER;
        }

        BarterResult result = tryStartBarter(serverLevel);
        if (result == BarterResult.SUCCESS) {
            goldStack.shrink(1);
            player.sendSystemMessage(Component.translatable("message.trading_cells.piglin_barter_started"));
            return InteractionResult.SUCCESS_SERVER;
        }

        player.sendSystemMessage(Component.translatable(result.messageKey));
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult extractOutputToPlayer(Player player) {
        if (outputBuffer.isEmpty()) {
            return InteractionResult.PASS;
        }

        ItemStack remaining = outputBuffer.copy();
        player.getInventory().add(remaining);
        outputBuffer = remaining.isEmpty() ? ItemStack.EMPTY : remaining;
        markChangedAndSync();

        if (outputBuffer.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.piglin_barter_output_taken"));
        } else {
            player.sendSystemMessage(Component.translatable("message.trading_cells.inventory_full"));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private void tickBarter(ServerLevel serverLevel) {
        if (isBartering()) {
            barterTicksRemaining--;
            if (barterTicksRemaining <= 0) {
                barterTicksRemaining = 0;
                finishBarter(serverLevel);
            }
            markChangedAndSync();
            return;
        }

        if (!outputBuffer.isEmpty()) {
            tryPushOutputBelow(serverLevel);
            return;
        }

        if (!goldBuffer.isEmpty() && goldBuffer.is(Items.GOLD_INGOT)) {
            BarterResult result = tryStartBarter(serverLevel);
            if (result == BarterResult.SUCCESS) {
                goldBuffer.shrink(1);
                if (goldBuffer.isEmpty()) {
                    goldBuffer = ItemStack.EMPTY;
                }
                markChangedAndSync();
            }
        }
    }

    private BarterResult tryStartBarter(ServerLevel serverLevel) {
        if (isBartering()) {
            return BarterResult.BUSY;
        }

        if (!outputBuffer.isEmpty()) {
            return BarterResult.OUTPUT_PENDING;
        }

        if (!hasPiglin()) {
            return BarterResult.EMPTY_CELL;
        }

        if (storedPiglinData == null) {
            return BarterResult.INVALID_PIGLIN;
        }

        if (PiglinCapturerItem.isBabyPiglin(storedPiglinData)) {
            return BarterResult.BABY_PIGLIN;
        }

        barterTicksRemaining = BARTER_TIME_TICKS;
        markChangedAndSync();
        return BarterResult.SUCCESS;
    }

    private void finishBarter(ServerLevel serverLevel) {
        if (!hasPiglin() || storedPiglinData == null || PiglinCapturerItem.isBabyPiglin(storedPiglinData)) {
            return;
        }

        Piglin piglin = PiglinCapturerItem.createCapturedPiglin(serverLevel, storedPiglinData, worldPosition);
        if (piglin == null) {
            return;
        }

        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.PIGLIN_BARTERING);
        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, piglin)
                .create(LootContextParamSets.PIGLIN_BARTER);
        List<ItemStack> barteredItems = lootTable.getRandomItems(lootParams);

        for (ItemStack result : barteredItems) {
            storeBarterResult(result.copy());
        }

        markChangedAndSync();
    }

    private void storeBarterResult(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        if (outputBuffer.isEmpty()) {
            outputBuffer = stack.copy();
            return;
        }

        if (ItemStack.isSameItemSameComponents(outputBuffer, stack) && outputBuffer.getCount() < outputBuffer.getMaxStackSize()) {
            int room = outputBuffer.getMaxStackSize() - outputBuffer.getCount();
            int moved = Math.min(room, stack.getCount());
            outputBuffer.grow(moved);
            stack.shrink(moved);
        }
    }

    private void tryPushOutputBelow(ServerLevel serverLevel) {
        if (outputBuffer.isEmpty()) {
            return;
        }

        ItemStack remainder = tryInsertBelow(serverLevel, outputBuffer.copy());
        if (remainder.getCount() != outputBuffer.getCount()) {
            outputBuffer = remainder.isEmpty() ? ItemStack.EMPTY : remainder;
            markChangedAndSync();
        }
    }

    private ItemStack tryInsertBelow(ServerLevel serverLevel, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        BlockPos belowPos = worldPosition.below();
        Container container = HopperBlockEntity.getContainerAt(serverLevel, belowPos);
        if (container == null) {
            return stack;
        }

        ItemStack remainder = HopperBlockEntity.addItem(null, container, stack, Direction.UP);
        container.setChanged();
        return remainder;
    }

    public void dropStoredPiglinCapturer(Level level, BlockPos pos) {
        CompoundTag piglinData = copyPiglinData();
        if (piglinData != null) {
            ItemStack drop = new ItemStack(TradingCellsRegistrationAdapter.PIGLIN_CAPTURER_ITEM.get());
            PiglinCapturerItem.setCapturedPiglinData(drop, piglinData);
            Block.popResource(level, pos, drop);
            clearStoredPiglin();
        }

        if (!goldBuffer.isEmpty()) {
            Block.popResource(level, pos, goldBuffer.copy());
            goldBuffer = ItemStack.EMPTY;
            setChanged();
        }

        if (isBartering()) {
            Block.popResource(level, pos, new ItemStack(Items.GOLD_INGOT));
            barterTicksRemaining = 0;
            setChanged();
        }

        if (!outputBuffer.isEmpty()) {
            Block.popResource(level, pos, outputBuffer.copy());
            outputBuffer = ItemStack.EMPTY;
            setChanged();
        }
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        storedPiglinData = input.read(PIGLIN_DATA_TAG, CompoundTag.CODEC).orElse(null);
        if (storedPiglinData == null || storedPiglinData.isEmpty()) {
            storedPiglinData = null;
        }

        goldBuffer = input.read(GOLD_BUFFER_TAG, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (!goldBuffer.is(Items.GOLD_INGOT)) {
            goldBuffer = ItemStack.EMPTY;
        }
        if (goldBuffer.getCount() > 1) {
            goldBuffer.setCount(1);
        }

        outputBuffer = input.read(OUTPUT_BUFFER_TAG, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        barterTicksRemaining = Math.max(0, input.getIntOr(BARTER_TICKS_TAG, 0));
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        if (storedPiglinData != null && !storedPiglinData.isEmpty()) {
            output.store(PIGLIN_DATA_TAG, CompoundTag.CODEC, storedPiglinData);
        }
        if (!goldBuffer.isEmpty()) {
            output.store(GOLD_BUFFER_TAG, ItemStack.CODEC, goldBuffer);
        }
        if (!outputBuffer.isEmpty()) {
            output.store(OUTPUT_BUFFER_TAG, ItemStack.CODEC, outputBuffer);
        }
        if (barterTicksRemaining > 0) {
            output.putInt(BARTER_TICKS_TAG, barterTicksRemaining);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return saveCustomOnly(registries);
    }

    private void clearStoredPiglin() {
        storedPiglinData = null;
        markChangedAndSync();
    }

    private void markChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return goldBuffer.isEmpty() && outputBuffer.isEmpty();
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        if (slot == GOLD_SLOT) {
            return goldBuffer;
        }
        if (slot == OUTPUT_SLOT) {
            return outputBuffer;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int count) {
        if (count <= 0) {
            return ItemStack.EMPTY;
        }

        if (slot == GOLD_SLOT && !goldBuffer.isEmpty()) {
            ItemStack removed = goldBuffer.split(count);
            if (goldBuffer.isEmpty()) {
                goldBuffer = ItemStack.EMPTY;
            }
            setChanged();
            return removed;
        }

        if (slot == OUTPUT_SLOT && !outputBuffer.isEmpty()) {
            ItemStack removed = outputBuffer.split(count);
            if (outputBuffer.isEmpty()) {
                outputBuffer = ItemStack.EMPTY;
            }
            setChanged();
            return removed;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        if (slot == GOLD_SLOT) {
            ItemStack removed = goldBuffer;
            goldBuffer = ItemStack.EMPTY;
            return removed;
        }

        if (slot == OUTPUT_SLOT) {
            ItemStack removed = outputBuffer;
            outputBuffer = ItemStack.EMPTY;
            return removed;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack stack) {
        if (slot == GOLD_SLOT) {
            if (stack.isEmpty()) {
                goldBuffer = ItemStack.EMPTY;
            } else if (stack.is(Items.GOLD_INGOT) && !isBartering() && outputBuffer.isEmpty() && goldBuffer.isEmpty()) {
                goldBuffer = stack.copy();
                goldBuffer.setCount(1);
            }
            setChanged();
            return;
        }

        if (slot == OUTPUT_SLOT) {
            outputBuffer = stack.copy();
            setChanged();
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, @NonNull ItemStack stack) {
        return slot == GOLD_SLOT && stack.is(Items.GOLD_INGOT) && !isBartering() && outputBuffer.isEmpty() && goldBuffer.isEmpty();
    }

    @Override
    public void clearContent() {
        goldBuffer = ItemStack.EMPTY;
        outputBuffer = ItemStack.EMPTY;
        setChanged();
    }

    @Override
    public int @NonNull [] getSlotsForFace(@NonNull Direction direction) {
        if (direction == Direction.UP) {
            return TOP_SLOTS;
        }
        if (direction == Direction.DOWN) {
            return BOTTOM_SLOTS;
        }
        return NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NonNull ItemStack stack, @Nullable Direction direction) {
        return direction == Direction.UP && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NonNull ItemStack stack, @NonNull Direction direction) {
        return direction == Direction.DOWN && slot == OUTPUT_SLOT;
    }

    private enum BarterResult {
        SUCCESS("message.trading_cells.piglin_bartered"),
        BUSY("message.trading_cells.piglin_barter_busy"),
        OUTPUT_PENDING("message.trading_cells.piglin_barter_output_pending"),
        EMPTY_CELL("message.trading_cells.cell_empty"),
        BABY_PIGLIN("message.trading_cells.baby_piglin_cannot_barter"),
        INVALID_PIGLIN("message.trading_cells.invalid_piglin"),
        FAILED("message.trading_cells.piglin_barter_failed");

        private final String messageKey;

        BarterResult(String messageKey) {
            this.messageKey = messageKey;
        }
    }
}
