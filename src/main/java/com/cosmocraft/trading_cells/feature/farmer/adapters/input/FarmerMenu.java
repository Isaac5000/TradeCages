package com.cosmocraft.trading_cells.feature.farmer.adapters.input;

import com.cosmocraft.trading_cells.feature.farmer.adapters.output.FarmerRegistrationAdapter;
import com.cosmocraft.trading_cells.feature.farmer.domain.model.FarmerCycle;
import com.cosmocraft.trading_cells.feature.incubators.adapters.input.CapturedMobStackAdapter;
import com.cosmocraft.trading_cells.feature.incubators.domain.model.IncubatorKind;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public final class FarmerMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = FarmerBlockEntity.CONTAINER_SIZE;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final Container container;
    private final ContainerData data;

    public FarmerMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(MACHINE_SLOT_COUNT), new SimpleContainerData(2));
    }

    public FarmerMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(FarmerRegistrationAdapter.FARMER_MENU.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.data = data;

        addSlot(new VillagerSlot(container, FarmerBlockEntity.VILLAGER_SLOT, 43, 30));
        addSlot(new CropSlot(container, FarmerBlockEntity.CROP_SLOT, 103, 30));
        addSlot(new HoeSlot(container, FarmerBlockEntity.HOE_SLOT, 73, 30));
        addSlot(new OutputSlot(container, FarmerBlockEntity.FIRST_OUTPUT_SLOT, 43, 94));
        addSlot(new OutputSlot(container, FarmerBlockEntity.FIRST_OUTPUT_SLOT + 1, 67, 94));
        addSlot(new OutputSlot(container, FarmerBlockEntity.FIRST_OUTPUT_SLOT + 2, 91, 94));
        addSlot(new OutputSlot(container, FarmerBlockEntity.FIRST_OUTPUT_SLOT + 3, 115, 94));
        addStandardInventorySlots(inventory, 8, 140);
        addDataSlots(data);
    }

    public int growthTicks() {
        return data.get(0);
    }

    public int maxGrowthTicks() {
        return Math.max(1, data.get(1));
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return container.stillValid(player);
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (isAdultVillager(stack)) {
            if (!moveItemStackTo(stack, FarmerBlockEntity.VILLAGER_SLOT, FarmerBlockEntity.VILLAGER_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (FarmerCropStackAdapter.isSupported(stack)) {
            if (!moveItemStackTo(stack, FarmerBlockEntity.CROP_SLOT, FarmerBlockEntity.CROP_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof HoeItem) {
            if (!moveItemStackTo(stack, FarmerBlockEntity.HOE_SLOT, FarmerBlockEntity.HOE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < PLAYER_INVENTORY_END) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_END, PLAYER_HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    private static boolean isAdultVillager(ItemStack stack) {
        return CapturedMobStackAdapter.isFilledCapturer(IncubatorKind.VILLAGER, stack)
                && !CapturedMobStackAdapter.isBaby(IncubatorKind.VILLAGER, stack);
    }

    private static final class VillagerSlot extends Slot {
        private VillagerSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NonNull ItemStack stack) {
            return isAdultVillager(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class CropSlot extends Slot {
        private CropSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NonNull ItemStack stack) {
            return FarmerCropStackAdapter.isSupported(stack);
        }
    }

    private static final class HoeSlot extends Slot {
        private HoeSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NonNull ItemStack stack) {
            return stack.getItem() instanceof HoeItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class OutputSlot extends Slot {
        private OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NonNull ItemStack stack) {
            return false;
        }
    }
}
