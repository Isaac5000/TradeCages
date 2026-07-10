package com.example.examplemod.feature.breeders.adapters.input;

import com.example.examplemod.feature.breeders.adapters.output.BreederRegistrationAdapter;
import com.example.examplemod.feature.breeders.domain.model.BreederKind;
import com.example.examplemod.feature.breeders.domain.model.BreederRecipe;
import com.example.examplemod.feature.tradecages.adapters.input.PiglinCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.output.TradingCellsRegistrationAdapter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public final class BreederMenu extends AbstractContainerMenu {
    private static final int BREEDER_SLOT_COUNT = BreederBlockEntity.CONTAINER_SIZE;
    private static final int PLAYER_INVENTORY_START = BREEDER_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final BreederKind kind;
    private final Container container;
    private final ContainerData data;

    public static BreederMenu villager(int containerId, Inventory inventory) {
        return new BreederMenu(BreederKind.VILLAGER, containerId, inventory, new SimpleContainer(BREEDER_SLOT_COUNT), new SimpleContainerData(2));
    }

    public static BreederMenu piglin(int containerId, Inventory inventory) {
        return new BreederMenu(BreederKind.PIGLIN, containerId, inventory, new SimpleContainer(BREEDER_SLOT_COUNT), new SimpleContainerData(2));
    }

    public BreederMenu(BreederKind kind, int containerId, Inventory inventory, Container container, ContainerData data) {
        super(kind == BreederKind.VILLAGER
                ? BreederRegistrationAdapter.VILLAGER_BREEDER_MENU.get()
                : BreederRegistrationAdapter.PIGLIN_BREEDER_MENU.get(), containerId);
        checkContainerSize(container, BREEDER_SLOT_COUNT);
        checkContainerDataCount(data, 2);
        this.kind = kind;
        this.container = container;
        this.data = data;

        addSlot(new FilteredSlot(container, BreederBlockEntity.FOOD_SLOT, 80, 16, this::isValidFood));
        addSlot(new FilteredSlot(container, BreederBlockEntity.PARENT_A_SLOT, 41, 48, this::isValidAdultParent));
        addSlot(new FilteredSlot(container, BreederBlockEntity.PARENT_B_SLOT, 116, 48, this::isValidAdultParent));
        addSlot(new PreviewSlot(container, BreederBlockEntity.BABY_PREVIEW_SLOT, 80, 79));
        addSlot(new FilteredSlot(container, BreederBlockEntity.EMPTY_CAPTURER_SLOT, 45, 115, this::isEmptyCapturer));
        addSlot(new OutputSlot(container, BreederBlockEntity.FILLED_CAPTURER_SLOT, 115, 115));

        addStandardInventorySlots(inventory, 8, 140);
        addDataSlots(data);
    }

    public BreederKind kind() {
        return kind;
    }

    public int breedTicks() {
        return data.get(0);
    }

    public int pendingBabies() {
        return data.get(1);
    }

    public int maxBreedTicks() {
        return BreederRecipe.breedTicks(kind);
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return container.stillValid(player);
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        if (index == BreederBlockEntity.BABY_PREVIEW_SLOT) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < BREEDER_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (isValidFood(stack)) {
            if (!moveItemStackTo(stack, BreederBlockEntity.FOOD_SLOT, BreederBlockEntity.FOOD_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (isValidAdultParent(stack)) {
            if (!moveItemStackTo(stack, BreederBlockEntity.PARENT_A_SLOT, BreederBlockEntity.PARENT_B_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (isEmptyCapturer(stack)) {
            if (!moveItemStackTo(stack, BreederBlockEntity.EMPTY_CAPTURER_SLOT, BreederBlockEntity.EMPTY_CAPTURER_SLOT + 1, false)) {
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

    private boolean isValidFood(ItemStack stack) {
        return BreederRecipe.isFood(kind, stack);
    }

    private boolean isValidAdultParent(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(capturerItem())) {
            return false;
        }
        if (kind == BreederKind.VILLAGER) {
            CompoundTag data = VillagerCapturerItem.getCapturedVillagerData(stack);
            return data != null && !VillagerCapturerItem.isBabyVillager(data);
        }
        CompoundTag data = PiglinCapturerItem.getCapturedPiglinData(stack);
        return data != null && !PiglinCapturerItem.isBabyPiglin(data);
    }

    private boolean isEmptyCapturer(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(capturerItem())) {
            return false;
        }
        return kind == BreederKind.VILLAGER
                ? !VillagerCapturerItem.hasCapturedVillager(stack)
                : !PiglinCapturerItem.hasCapturedPiglin(stack);
    }

    private Item capturerItem() {
        return kind == BreederKind.VILLAGER
                ? TradingCellsRegistrationAdapter.VILLAGER_CAPTURER_ITEM.get()
                : TradingCellsRegistrationAdapter.PIGLIN_CAPTURER_ITEM.get();
    }

    @FunctionalInterface
    private interface StackFilter {
        boolean test(ItemStack stack);
    }

    private static final class FilteredSlot extends Slot {
        private final StackFilter filter;

        private FilteredSlot(Container container, int slot, int x, int y, StackFilter filter) {
            super(container, slot, x, y);
            this.filter = filter;
        }

        @Override
        public boolean mayPlace(@NonNull ItemStack stack) {
            return filter.test(stack);
        }
    }

    private static final class PreviewSlot extends Slot {
        private PreviewSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NonNull ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(@NonNull Player player) {
            return false;
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
