package com.cosmocraft.trading_cells.feature.farmer.adapters.input;

import com.cosmocraft.trading_cells.feature.farmer.adapters.output.FarmerRegistrationAdapter;
import com.cosmocraft.trading_cells.feature.farmer.domain.model.FarmerCrop;
import com.cosmocraft.trading_cells.feature.farmer.domain.model.FarmerCycle;
import com.cosmocraft.trading_cells.feature.farmer.domain.model.FarmerHarvest;
import com.cosmocraft.trading_cells.feature.incubators.adapters.input.CapturedMobStackAdapter;
import com.cosmocraft.trading_cells.feature.incubators.domain.model.IncubatorKind;
import com.cosmocraft.trading_cells.feature.machines.application.MachineSettings;
import com.cosmocraft.trading_cells.platform.neoforge.machine.PortableMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class FarmerBlockEntity extends PortableMachineBlockEntity implements WorldlyContainer, MenuProvider {
    public static final int VILLAGER_SLOT = 0;
    public static final int CROP_SLOT = 1;
    public static final int HOE_SLOT = 2;
    public static final int FIRST_OUTPUT_SLOT = 3;
    public static final int OUTPUT_SLOT_COUNT = 4;
    public static final int CONTAINER_SIZE = FIRST_OUTPUT_SLOT + OUTPUT_SLOT_COUNT;

    private static final String SLOT_TAG_PREFIX = "Slot";
    private static final String GROWTH_TICKS_TAG = "GrowthTicks";
    private static final int[] TOP_SLOTS = new int[]{CROP_SLOT};
    private static final int[] SIDE_SLOTS = new int[]{VILLAGER_SLOT, HOE_SLOT};
    private static final int[] BOTTOM_SLOTS = new int[]{3, 4, 5, 6};
    private static final int[] NO_SLOTS = new int[0];

    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int growthTicks;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> growthTicks;
                case 1 -> FarmerCycle.growthTicks();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                growthTicks = Math.max(0, Math.min(FarmerCycle.growthTicks(), value));
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public FarmerBlockEntity(BlockPos pos, BlockState state) {
        super(FarmerRegistrationAdapter.FARMER_BLOCK_ENTITY.get(), pos, state);
    }

    public ContainerData dataAccess() {
        return dataAccess;
    }

    public int growthTicks() {
        return growthTicks;
    }

    public FarmerCrop crop() {
        return FarmerCropStackAdapter.from(items.get(CROP_SLOT));
    }

    @Override
    public void processTick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        FarmerCrop crop = crop();
        if (!isAdultVillager(items.get(VILLAGER_SLOT)) || crop == FarmerCrop.NONE) {
            resetProgress();
            return;
        }

        FarmerHarvest harvest = FarmerCycle.harvest(crop, fortuneLevel());
        if (!canStoreHarvest(crop, harvest)) {
            return;
        }

        int efficiencyBonus = efficiencyLevel() * MachineSettings.values().farmerEfficiencyBonusPerLevel();
        growthTicks = Math.min(FarmerCycle.growthTicks(), growthTicks + 1 + efficiencyBonus);
        if (growthTicks < FarmerCycle.growthTicks()) {
            setChanged();
            if (growthTicks % 20 == 0) {
                markChangedAndSync();
            }
            return;
        }

        storeOutput(FarmerCropStackAdapter.produce(crop, harvest.produceCount()));
        storeOutput(FarmerCropStackAdapter.seeds(crop, harvest.seedCount()));
        growthTicks = 0;
        markChangedAndSync();
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable("container.trading_cells.farmer");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NonNull Inventory inventory, @NonNull Player player) {
        return new FarmerMenu(containerId, inventory, this, dataAccess);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        return isValidSlot(slot) ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int count) {
        if (!isValidSlot(slot) || count <= 0 || items.get(slot).isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = items.get(slot).split(count);
        if (items.get(slot).isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
        if (slot == VILLAGER_SLOT || (slot == CROP_SLOT && items.get(slot).isEmpty())) {
            growthTicks = 0;
        }
        markChangedAndSync();
        return removed;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        if (!isValidSlot(slot)) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        if (slot == VILLAGER_SLOT || slot == CROP_SLOT) {
            growthTicks = 0;
        }
        return removed;
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack stack) {
        if (!isValidSlot(slot)) {
            return;
        }
        if (!stack.isEmpty() && !canPlaceItem(slot, stack)) {
            return;
        }
        FarmerCrop previousCrop = crop();
        ItemStack inserted = stack.copy();
        int max = slot == VILLAGER_SLOT || slot == HOE_SLOT ? 1 : Math.min(64, inserted.getMaxStackSize());
        inserted.setCount(Math.min(max, inserted.getCount()));
        items.set(slot, inserted);
        if (slot == VILLAGER_SLOT || (slot == CROP_SLOT && previousCrop != crop())) {
            growthTicks = 0;
        }
        markChangedAndSync();
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, @NonNull ItemStack stack) {
        return switch (slot) {
            case VILLAGER_SLOT -> isAdultVillager(stack);
            case CROP_SLOT -> FarmerCropStackAdapter.isSupported(stack);
            case HOE_SLOT -> stack.getItem() instanceof HoeItem;
            default -> false;
        };
    }

    @Override
    public void clearContent() {
        clearContentsForBlockDrop();
        markChangedAndSync();
    }

    @Override
    public int @NonNull [] getSlotsForFace(@NonNull Direction direction) {
        if (direction == Direction.UP) {
            return TOP_SLOTS;
        }
        if (direction == Direction.DOWN) {
            return BOTTOM_SLOTS;
        }
        return direction.getAxis().isHorizontal() ? SIDE_SLOTS : NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NonNull ItemStack stack, @Nullable Direction direction) {
        return direction != Direction.DOWN && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NonNull ItemStack stack, @NonNull Direction direction) {
        return direction == Direction.DOWN && isOutputSlot(slot);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            items.set(slot, input.read(SLOT_TAG_PREFIX + slot, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        }
        growthTicks = Math.max(0, Math.min(FarmerCycle.growthTicks(), input.getIntOr(GROWTH_TICKS_TAG, 0)));
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            if (!items.get(slot).isEmpty()) {
                output.store(SLOT_TAG_PREFIX + slot, ItemStack.CODEC, items.get(slot));
            }
        }
        if (growthTicks > 0) {
            output.putInt(GROWTH_TICKS_TAG, growthTicks);
        }
    }

    @Override
    protected void clearContentsForBlockDrop() {
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            items.set(slot, ItemStack.EMPTY);
        }
        growthTicks = 0;
        setChanged();
    }

    private int fortuneLevel() {
        ItemStack hoe = items.get(HOE_SLOT);
        if (hoe.isEmpty() || level == null) {
            return 0;
        }
        var fortune = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        return hoe.getEnchantmentLevel(fortune);
    }

    private int efficiencyLevel() {
        ItemStack hoe = items.get(HOE_SLOT);
        if (hoe.isEmpty() || level == null) {
            return 0;
        }
        var efficiency = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
        return hoe.getEnchantmentLevel(efficiency);
    }

    private boolean canStoreHarvest(FarmerCrop crop, FarmerHarvest harvest) {
        NonNullList<ItemStack> simulated = NonNullList.withSize(OUTPUT_SLOT_COUNT, ItemStack.EMPTY);
        for (int index = 0; index < OUTPUT_SLOT_COUNT; index++) {
            simulated.set(index, items.get(FIRST_OUTPUT_SLOT + index).copy());
        }
        return merge(simulated, FarmerCropStackAdapter.produce(crop, harvest.produceCount()))
                && merge(simulated, FarmerCropStackAdapter.seeds(crop, harvest.seedCount()));
    }

    private void storeOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        NonNullList<ItemStack> outputs = NonNullList.withSize(OUTPUT_SLOT_COUNT, ItemStack.EMPTY);
        for (int index = 0; index < OUTPUT_SLOT_COUNT; index++) {
            outputs.set(index, items.get(FIRST_OUTPUT_SLOT + index));
        }
        merge(outputs, stack);
        for (int index = 0; index < OUTPUT_SLOT_COUNT; index++) {
            items.set(FIRST_OUTPUT_SLOT + index, outputs.get(index));
        }
    }

    private static boolean merge(NonNullList<ItemStack> outputs, ItemStack source) {
        if (source.isEmpty()) {
            return true;
        }
        ItemStack remaining = source.copy();
        for (ItemStack output : outputs) {
            if (!output.isEmpty() && ItemStack.isSameItemSameComponents(output, remaining)) {
                int moved = Math.min(remaining.getCount(), output.getMaxStackSize() - output.getCount());
                output.grow(moved);
                remaining.shrink(moved);
                if (remaining.isEmpty()) {
                    return true;
                }
            }
        }
        for (int index = 0; index < outputs.size(); index++) {
            if (outputs.get(index).isEmpty()) {
                int moved = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                ItemStack inserted = remaining.copy();
                inserted.setCount(moved);
                outputs.set(index, inserted);
                remaining.shrink(moved);
                if (remaining.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void resetProgress() {
        if (growthTicks != 0) {
            growthTicks = 0;
            markChangedAndSync();
        }
    }

    private static boolean isAdultVillager(ItemStack stack) {
        return CapturedMobStackAdapter.isFilledCapturer(IncubatorKind.VILLAGER, stack)
                && !CapturedMobStackAdapter.isBaby(IncubatorKind.VILLAGER, stack);
    }

    private static boolean isOutputSlot(int slot) {
        return slot >= FIRST_OUTPUT_SLOT && slot < CONTAINER_SIZE;
    }

    private static boolean isValidSlot(int slot) {
        return slot >= 0 && slot < CONTAINER_SIZE;
    }
}
