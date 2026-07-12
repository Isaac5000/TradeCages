package com.cosmocraft.trading_cells.feature.autotrader.adapters.input;

import com.cosmocraft.trading_cells.feature.autotrader.adapters.output.AutotraderRegistrationAdapter;
import com.cosmocraft.trading_cells.feature.autotrader.domain.model.AutotraderPolicy;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.VillagerCapturerItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class AutotraderMenu extends AbstractContainerMenu {
    public static final int EXTRACT_EXPERIENCE_BUTTON = 1;
    public static final int SELECT_OFFER_BUTTON_BASE = 100;
    public static final int INPUT_ROW_X = 44;
    public static final int INPUT_A_ROW_Y = 36;
    public static final int INPUT_B_ROW_Y = 60;
    public static final int OUTPUT_ROW_Y = 84;
    private static final int MACHINE_SLOT_COUNT = AutotraderBlockEntity.CONTAINER_SIZE;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final Container container;
    private final ContainerData data;
    private ItemStack cachedVillagerStack = ItemStack.EMPTY;
    private MerchantOffers cachedOffers = new MerchantOffers();

    public AutotraderMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(MACHINE_SLOT_COUNT), new SimpleContainerData(3));
    }

    public AutotraderMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(AutotraderRegistrationAdapter.AUTOTRADER_MENU.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;
        addSlot(new HiddenVillagerSlot(container, AutotraderBlockEntity.VILLAGER_SLOT));
        for (int index = 0; index < AutotraderPolicy.INPUT_SLOTS_PER_COST; index++) {
            addSlot(new InputASlot(
                    container,
                    AutotraderBlockEntity.FIRST_INPUT_A_SLOT + index,
                    INPUT_ROW_X + index * 24,
                    INPUT_A_ROW_Y
            ));
        }
        for (int index = 0; index < AutotraderPolicy.INPUT_SLOTS_PER_COST; index++) {
            addSlot(new InputBSlot(
                    container,
                    AutotraderBlockEntity.FIRST_INPUT_B_SLOT + index,
                    INPUT_ROW_X + index * 24,
                    INPUT_B_ROW_Y
            ));
        }
        for (int index = 0; index < AutotraderPolicy.OUTPUT_SLOTS; index++) {
            addSlot(new OutputSlot(
                    container,
                    AutotraderBlockEntity.FIRST_OUTPUT_SLOT + index,
                    INPUT_ROW_X + index * 24,
                    OUTPUT_ROW_Y
            ));
        }
        addStandardInventorySlots(inventory, 8, 140);
        addDataSlots(data);
    }

    public int selectedOfferIndex() {
        return data.get(0);
    }

    public int offerCount() {
        return data.get(1);
    }

    public int storedExperience() {
        return data.get(2);
    }

    public @Nullable MerchantOffer selectedOffer() {
        refreshOffers();
        if (cachedOffers.isEmpty()) {
            return null;
        }
        return cachedOffers.get(Math.floorMod(selectedOfferIndex(), cachedOffers.size()));
    }

    public MerchantOffers offers() {
        refreshOffers();
        return cachedOffers;
    }

    public boolean hasVillager() {
        return VillagerCapturerItem.hasCapturedVillager(container.getItem(AutotraderBlockEntity.VILLAGER_SLOT));
    }

    @Override
    public boolean clickMenuButton(@NonNull Player player, int buttonId) {
        if (buttonId == EXTRACT_EXPERIENCE_BUTTON) {
            if (container instanceof AutotraderBlockEntity autotrader) {
                autotrader.extractExperience(player);
                return true;
            }
            return false;
        }
        int offerIndex = buttonId - SELECT_OFFER_BUTTON_BASE;
        if (offerIndex < 0 || offerIndex >= data.get(1)) {
            return false;
        }
        data.set(0, offerIndex);
        container.setChanged();
        return true;
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
        if (index == AutotraderBlockEntity.VILLAGER_SLOT) {
            return ItemStack.EMPTY;
        } else if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            MerchantOffer offer = selectedOffer();
            if (offer != null && offer.getItemCostA().test(stack)) {
                if (!moveItemStackTo(stack, AutotraderBlockEntity.FIRST_INPUT_A_SLOT, AutotraderBlockEntity.FIRST_INPUT_B_SLOT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (offer != null && offer.getItemCostB().map(cost -> cost.test(stack)).orElse(false)) {
                if (!moveItemStackTo(stack, AutotraderBlockEntity.FIRST_INPUT_B_SLOT, AutotraderBlockEntity.FIRST_OUTPUT_SLOT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < PLAYER_INVENTORY_END) {
                if (!moveItemStackTo(stack, PLAYER_INVENTORY_END, PLAYER_HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    private void refreshOffers() {
        ItemStack villagerStack = container.getItem(AutotraderBlockEntity.VILLAGER_SLOT);
        boolean stackChanged = !ItemStack.isSameItemSameComponents(villagerStack, cachedVillagerStack);
        if (!stackChanged && cachedOffers.size() == offerCount()) {
            return;
        }
        CompoundTag villagerData = VillagerCapturerItem.getCapturedVillagerData(villagerStack);
        if (villagerData == null) {
            cachedVillagerStack = villagerStack.copy();
            cachedOffers = new MerchantOffers();
            return;
        }
        var decodedOffers = villagerData.read("Offers", MerchantOffers.CODEC);
        decodedOffers.ifPresent(offers -> {
            cachedVillagerStack = villagerStack.copy();
            cachedOffers = offers.copy();
        });
        if (decodedOffers.isEmpty() && offerCount() == 0) {
            cachedVillagerStack = villagerStack.copy();
            cachedOffers = new MerchantOffers();
        }
    }

    private static final class HiddenVillagerSlot extends Slot {
        private HiddenVillagerSlot(Container container, int slot) {
            super(container, slot, -1_000, -1_000);
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

    private final class InputASlot extends Slot {
        private InputASlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NonNull ItemStack stack) {
            MerchantOffer offer = selectedOffer();
            return offer != null && offer.getItemCostA().test(stack);
        }
    }

    private final class InputBSlot extends Slot {
        private InputBSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NonNull ItemStack stack) {
            MerchantOffer offer = selectedOffer();
            return offer != null && offer.getItemCostB().map(cost -> cost.test(stack)).orElse(false);
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
