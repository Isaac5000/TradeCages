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
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
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
    private static final int GOLD_SLOT = 0;
    private static final int[] TOP_SLOTS = new int[]{GOLD_SLOT};
    private static final int[] NO_SLOTS = new int[0];

    private @Nullable CompoundTag storedPiglinData;
    private ItemStack goldBuffer = ItemStack.EMPTY;

    public PiglinBarteringCellBlockEntity(BlockPos pos, BlockState blockState) {
        super(TradingCellsRegistrationAdapter.PIGLIN_BARTERING_CELL_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PiglinBarteringCellBlockEntity cell) {
        if (level instanceof ServerLevel serverLevel) {
            cell.processGoldBuffer(serverLevel);
        }
    }

    public boolean hasPiglin() {
        return storedPiglinData != null && !storedPiglinData.isEmpty();
    }

    public @Nullable CompoundTag copyPiglinData() {
        return hasPiglin() && storedPiglinData != null ? storedPiglinData.copy() : null;
    }

    public @Nullable Piglin createPiglinForDisplay() {
        if (level == null || !hasPiglin() || storedPiglinData == null) {
            return null;
        }
        return PiglinCapturerItem.createCapturedPiglin(level, storedPiglinData, worldPosition);
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

        BarterResult result = barterOnce(serverLevel, player);
        if (result == BarterResult.SUCCESS) {
            goldStack.shrink(1);
            player.sendSystemMessage(Component.translatable("message.trading_cells.piglin_bartered"));
            return InteractionResult.SUCCESS_SERVER;
        }

        player.sendSystemMessage(Component.translatable(result.messageKey));
        return InteractionResult.SUCCESS_SERVER;
    }

    private void processGoldBuffer(ServerLevel serverLevel) {
        if (goldBuffer.isEmpty() || !goldBuffer.is(Items.GOLD_INGOT)) {
            return;
        }

        BarterResult result = barterOnce(serverLevel, null);
        if (result == BarterResult.SUCCESS) {
            goldBuffer.shrink(1);
            if (goldBuffer.isEmpty()) {
                goldBuffer = ItemStack.EMPTY;
            }
            setChanged();
        }
    }

    private BarterResult barterOnce(ServerLevel serverLevel, @Nullable Player player) {
        if (!hasPiglin()) {
            return BarterResult.EMPTY_CELL;
        }

        if (storedPiglinData == null) {
            return BarterResult.INVALID_PIGLIN;
        }

        if (PiglinCapturerItem.isBabyPiglin(storedPiglinData)) {
            return BarterResult.BABY_PIGLIN;
        }

        Piglin piglin = PiglinCapturerItem.createCapturedPiglin(serverLevel, storedPiglinData, worldPosition);
        if (piglin == null) {
            return BarterResult.INVALID_PIGLIN;
        }

        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.PIGLIN_BARTERING);
        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, piglin)
                .create(LootContextParamSets.PIGLIN_BARTER);
        List<ItemStack> barteredItems = lootTable.getRandomItems(lootParams);
        if (barteredItems.isEmpty()) {
            return BarterResult.FAILED;
        }

        for (ItemStack result : barteredItems) {
            deliverBarterResult(serverLevel, result.copy(), player);
        }

        return BarterResult.SUCCESS;
    }

    private void deliverBarterResult(ServerLevel serverLevel, ItemStack stack, @Nullable Player player) {
        ItemStack remainder = tryInsertBelow(serverLevel, stack);

        if (player != null && !remainder.isEmpty()) {
            player.getInventory().add(remainder);
        }

        if (!remainder.isEmpty()) {
            Block.popResource(serverLevel, worldPosition, remainder);
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
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
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
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return goldBuffer.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == GOLD_SLOT ? goldBuffer : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        if (slot != GOLD_SLOT || count <= 0 || goldBuffer.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = goldBuffer.split(count);
        if (goldBuffer.isEmpty()) {
            goldBuffer = ItemStack.EMPTY;
        }
        setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != GOLD_SLOT) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = goldBuffer;
        goldBuffer = ItemStack.EMPTY;
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != GOLD_SLOT) {
            return;
        }

        if (stack.isEmpty()) {
            goldBuffer = ItemStack.EMPTY;
        } else if (stack.is(Items.GOLD_INGOT)) {
            goldBuffer = stack.copy();
            if (goldBuffer.getCount() > getMaxStackSize(goldBuffer)) {
                goldBuffer.setCount(getMaxStackSize(goldBuffer));
            }
        }
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == GOLD_SLOT && stack.is(Items.GOLD_INGOT);
    }

    @Override
    public void clearContent() {
        goldBuffer = ItemStack.EMPTY;
        setChanged();
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return direction == Direction.UP ? TOP_SLOTS : NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return direction == Direction.UP && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }

    private enum BarterResult {
        SUCCESS("message.trading_cells.piglin_bartered"),
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
