package com.cosmocraft.trading_cells.feature.autotrader.adapters.input;

import com.cosmocraft.trading_cells.feature.autotrader.adapters.output.AutotraderRegistrationAdapter;
import com.cosmocraft.trading_cells.feature.autotrader.domain.model.AutotraderPolicy;
import com.cosmocraft.trading_cells.feature.incubators.adapters.input.CapturedMobStackAdapter;
import com.cosmocraft.trading_cells.feature.incubators.domain.model.IncubatorKind;
import com.cosmocraft.trading_cells.feature.machines.application.MachineSettings;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.VillagerPoiAdapter;
import com.cosmocraft.trading_cells.feature.tradecages.domain.model.VillagerOfferPersistence;
import com.cosmocraft.trading_cells.platform.neoforge.machine.AbstractPortableMachineBlock;
import com.cosmocraft.trading_cells.platform.neoforge.machine.PortableMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.TagValueInput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class AutotraderBlockEntity extends PortableMachineBlockEntity implements WorldlyContainer, MenuProvider {
    public static final int VILLAGER_SLOT = 0;
    public static final int FIRST_INPUT_A_SLOT = 1;
    public static final int FIRST_INPUT_B_SLOT = FIRST_INPUT_A_SLOT + AutotraderPolicy.INPUT_SLOTS_PER_COST;
    public static final int FIRST_OUTPUT_SLOT = FIRST_INPUT_B_SLOT + AutotraderPolicy.INPUT_SLOTS_PER_COST;
    public static final int CONTAINER_SIZE = FIRST_OUTPUT_SLOT + AutotraderPolicy.OUTPUT_SLOTS;

    private static final String SLOT_TAG_PREFIX = "Slot";
    private static final String SELECTED_OFFER_TAG = "SelectedOffer";
    private static final String POI_STACK_TAG = "StoredPoi";
    private static final String STORED_EXPERIENCE_TAG = "StoredExperience";
    private static final String OFFER_AGE_TICKS_TAG = "OfferAgeTicks";
    private static final String CURE_DISCOUNT_TAG = "TradingCellsCureDiscount";
    private static final int[] INPUT_A_SLOTS = new int[]{1, 2, 3, 4};
    private static final int[] INPUT_B_SLOTS = new int[]{5, 6, 7, 8};
    private static final int[] OUTPUT_SLOTS = new int[]{9, 10, 11, 12};
    private static final int[] NO_SLOTS = new int[0];

    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int selectedOfferIndex;
    private int storedExperience;
    private int offerAgeTicks;
    private ItemStack storedPoiStack = ItemStack.EMPTY;
    private @Nullable AutotraderVillager cachedVillager;
    private @Nullable CompoundTag cachedVillagerData;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> selectedOfferIndex;
                case 1 -> offerCount();
                case 2 -> storedExperience;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                int count = offerCount();
                selectedOfferIndex = count == 0 ? 0 : Math.floorMod(value, count);
                markChangedAndSync();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public AutotraderBlockEntity(BlockPos pos, BlockState state) {
        super(AutotraderRegistrationAdapter.AUTOTRADER_BLOCK_ENTITY.get(), pos, state);
    }

    public ContainerData dataAccess() {
        return dataAccess;
    }

    public boolean hasStoredVillager() {
        return isAdultVillager(items.get(VILLAGER_SLOT));
    }

    public ItemStack copyDisplayVillagerStack() {
        return items.get(VILLAGER_SLOT).copy();
    }

    public ItemStack copyPoiStack() {
        return storedPoiStack.copy();
    }

    public void extractExperience(Player player) {
        if (level == null || level.isClientSide() || storedExperience <= 0) {
            return;
        }
        int extracted = storedExperience;
        storedExperience = 0;
        player.giveExperiencePoints(extracted);
        markChangedAndSync();
    }

    public InteractionResult insertVillagerFromCapturer(ItemStack stack, Player player) {
        if (hasStoredVillager()) {
            return InteractionResult.SUCCESS_SERVER;
        }
        if (!isAdultVillager(stack)) {
            return InteractionResult.PASS;
        }

        items.set(VILLAGER_SLOT, stack.copyWithCount(1));
        VillagerCapturerItem.clearCapturedVillager(stack);
        selectedOfferIndex = 0;
        offerAgeTicks = 0;
        invalidateVillagerCache();
        refreshVillagerProfessionFromPoi();
        markChangedAndSync();
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult extractVillagerToCapturer(ItemStack stack, Player player) {
        if (!hasStoredVillager()) {
            return InteractionResult.SUCCESS_SERVER;
        }
        if (VillagerCapturerItem.hasCapturedVillager(stack)) {
            return InteractionResult.SUCCESS_SERVER;
        }

        persistCachedVillager();
        CompoundTag data = VillagerCapturerItem.getCapturedVillagerData(items.get(VILLAGER_SLOT));
        if (data == null) {
            return InteractionResult.FAIL;
        }
        ItemStack target = stack.getCount() <= 1 ? stack : new ItemStack(stack.getItem());
        VillagerCapturerItem.setCapturedVillagerData(target, data);
        if (target != stack) {
            stack.shrink(1);
            if (!player.getInventory().add(target)) {
                player.drop(target, false);
            }
        }

        items.set(VILLAGER_SLOT, ItemStack.EMPTY);
        selectedOfferIndex = 0;
        offerAgeTicks = 0;
        invalidateVillagerCache();
        markChangedAndSync();
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult insertPoiFromStack(ItemStack stack, Player player) {
        if (level == null) {
            return InteractionResult.PASS;
        }
        String professionId = VillagerPoiAdapter.professionFor(level, stack);
        if (professionId == null) {
            return InteractionResult.PASS;
        }

        persistCachedVillager();
        CompoundTag villagerData = VillagerCapturerItem.getCapturedVillagerData(items.get(VILLAGER_SLOT));
        if (!storedPoiStack.isEmpty()) {
            if (villagerData != null && VillagerPoiAdapter.hasPersistentProfession(villagerData)) {
                return InteractionResult.SUCCESS_SERVER;
            }
            ItemStack previous = storedPoiStack.copy();
            if (!player.getInventory().add(previous)) {
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        storedPoiStack = stack.copyWithCount(1);
        stack.shrink(1);
        refreshVillagerProfessionFromPoi();
        markChangedAndSync();
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult extractPoiToPlayer(Player player) {
        if (storedPoiStack.isEmpty()) {
            return InteractionResult.SUCCESS_SERVER;
        }
        ItemStack returned = storedPoiStack.copy();
        if (!player.getInventory().add(returned)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        storedPoiStack = ItemStack.EMPTY;
        clearTransientProfession();
        markChangedAndSync();
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void processTick() {
        if (level == null || level.isClientSide()) {
            return;
        }
        AutotraderVillager villager = resolveVillager();
        tickOfferAge(villager);
        MerchantOffer offer = selectedOffer(villager);
        if (villager == null || offer == null || offer.isOutOfStock()) {
            return;
        }

        ItemCost costA = offer.getItemCostA();
        @Nullable ItemCost costB = offer.getItemCostB().orElse(null);
        ItemStack result = offer.assemble();
        if (countMatching(INPUT_A_SLOTS, costA) < offer.getCostA().getCount()
                || costB != null && countMatching(INPUT_B_SLOTS, costB) < offer.getCostB().getCount()
                || !canStoreOutput(result)) {
            return;
        }

        consume(INPUT_A_SLOTS, costA, offer.getCostA().getCount());
        if (costB != null) {
            consume(INPUT_B_SLOTS, costB, offer.getCostB().getCount());
        }
        storeOutput(result);
        storedExperience = (int) Math.min(
                Integer.MAX_VALUE,
                (long) storedExperience + villager.completeAutomaticTrade(offer)
        );
        prepareAutomaticPrices(villager);
        persistCachedVillager();
        markChangedAndSync();
    }

    public @Nullable MerchantOffer selectedOffer() {
        return selectedOffer(resolveVillager());
    }

    private @Nullable MerchantOffer selectedOffer(@Nullable Villager villager) {
        if (villager == null) {
            return null;
        }
        MerchantOffers offers = villager.getOffers();
        if (offers.isEmpty()) {
            selectedOfferIndex = 0;
            return null;
        }
        selectedOfferIndex = Math.floorMod(selectedOfferIndex, offers.size());
        return offers.get(selectedOfferIndex);
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable("container.trading_cells.autotrader");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NonNull Inventory inventory, @NonNull Player player) {
        return new AutotraderMenu(containerId, inventory, this, dataAccess);
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
        if (slot == VILLAGER_SLOT) {
            invalidateVillagerCache();
            selectedOfferIndex = 0;
            offerAgeTicks = 0;
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
        if (slot == VILLAGER_SLOT) {
            invalidateVillagerCache();
            selectedOfferIndex = 0;
            offerAgeTicks = 0;
        }
        return removed;
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack stack) {
        if (!isValidSlot(slot) || !stack.isEmpty() && !canPlaceItem(slot, stack)) {
            return;
        }
        ItemStack inserted = stack.copy();
        if (slot == VILLAGER_SLOT) {
            inserted.setCount(Math.min(1, inserted.getCount()));
        } else {
            inserted.setCount(Math.min(inserted.getMaxStackSize(), inserted.getCount()));
        }
        items.set(slot, inserted);
        if (slot == VILLAGER_SLOT) {
            invalidateVillagerCache();
            selectedOfferIndex = 0;
            offerAgeTicks = 0;
        }
        markChangedAndSync();
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, @NonNull ItemStack stack) {
        if (slot == VILLAGER_SLOT) {
            return isAdultVillager(stack);
        }
        MerchantOffer offer = selectedOffer();
        if (offer == null) {
            return false;
        }
        if (isInputASlot(slot)) {
            return offer.getItemCostA().test(stack);
        }
        if (isInputBSlot(slot)) {
            return offer.getItemCostB().map(cost -> cost.test(stack)).orElse(false);
        }
        return false;
    }

    @Override
    public void clearContent() {
        clearContentsForBlockDrop();
        markChangedAndSync();
    }

    @Override
    public int @NonNull [] getSlotsForFace(@NonNull Direction direction) {
        if (direction == Direction.DOWN) {
            return OUTPUT_SLOTS;
        }
        Direction facing = getBlockState().getValue(AbstractPortableMachineBlock.FACING);
        if (direction == facing.getClockWise()) {
            return INPUT_A_SLOTS;
        }
        if (direction == facing.getCounterClockWise()) {
            return INPUT_B_SLOTS;
        }
        return NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NonNull ItemStack stack, @Nullable Direction direction) {
        if (direction == null) {
            return false;
        }
        for (int allowedSlot : getSlotsForFace(direction)) {
            if (allowedSlot == slot) {
                return canPlaceItem(slot, stack);
            }
        }
        return false;
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
        storedPoiStack = input.read(POI_STACK_TAG, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        selectedOfferIndex = Math.max(0, input.getIntOr(SELECTED_OFFER_TAG, 0));
        storedExperience = Math.max(0, input.getIntOr(STORED_EXPERIENCE_TAG, 0));
        offerAgeTicks = Math.max(0, Math.min(
                VillagerOfferPersistence.refreshIntervalTicks(),
                input.getIntOr(OFFER_AGE_TICKS_TAG, 0)
        ));
        invalidateVillagerCache();
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            if (!items.get(slot).isEmpty()) {
                output.store(SLOT_TAG_PREFIX + slot, ItemStack.CODEC, items.get(slot));
            }
        }
        if (selectedOfferIndex > 0) {
            output.putInt(SELECTED_OFFER_TAG, selectedOfferIndex);
        }
        if (!storedPoiStack.isEmpty()) {
            output.store(POI_STACK_TAG, ItemStack.CODEC, storedPoiStack);
        }
        if (storedExperience > 0) {
            output.putInt(STORED_EXPERIENCE_TAG, storedExperience);
        }
        if (offerAgeTicks > 0) {
            output.putInt(OFFER_AGE_TICKS_TAG, offerAgeTicks);
        }
    }

    @Override
    protected void beforeBlockDropSnapshot() {
        persistCachedVillager();
    }

    @Override
    protected void clearContentsForBlockDrop() {
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            items.set(slot, ItemStack.EMPTY);
        }
        storedPoiStack = ItemStack.EMPTY;
        selectedOfferIndex = 0;
        storedExperience = 0;
        offerAgeTicks = 0;
        invalidateVillagerCache();
        setChanged();
    }

    private int offerCount() {
        Villager villager = resolveVillager();
        return villager == null ? 0 : villager.getOffers().size();
    }

    private @Nullable AutotraderVillager resolveVillager() {
        if (level == null || level.isClientSide()) {
            return null;
        }
        CompoundTag currentData = VillagerCapturerItem.getCapturedVillagerData(items.get(VILLAGER_SLOT));
        if (currentData == null) {
            invalidateVillagerCache();
            return null;
        }
        if (cachedVillager != null && currentData.equals(cachedVillagerData)) {
            return cachedVillager;
        }

        AutotraderVillager villager = new AutotraderVillager(level);
        villager.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), currentData.copy()));
        villager.setPos(worldPosition.getX() + 0.5D, worldPosition.getY(), worldPosition.getZ() + 0.5D);
        villager.setPersistenceRequired();
        villager.getOffers();
        if (!level.isClientSide()) {
            prepareAutomaticPrices(villager);
        }
        cachedVillager = villager;
        cachedVillagerData = currentData.copy();
        persistCachedVillager();
        return cachedVillager;
    }

    private void refreshVillagerProfessionFromPoi() {
        persistCachedVillager();
        CompoundTag data = VillagerCapturerItem.getCapturedVillagerData(items.get(VILLAGER_SLOT));
        if (data == null || level == null) {
            return;
        }
        boolean changed = VillagerPoiAdapter.refreshProfession(
                data,
                VillagerPoiAdapter.professionFor(level, storedPoiStack)
        );
        VillagerCapturerItem.setCapturedVillagerData(items.get(VILLAGER_SLOT), data);
        if (changed) {
            selectedOfferIndex = 0;
            offerAgeTicks = 0;
            invalidateVillagerCache();
        }
    }

    private void clearTransientProfession() {
        persistCachedVillager();
        CompoundTag data = VillagerCapturerItem.getCapturedVillagerData(items.get(VILLAGER_SLOT));
        if (data == null) {
            return;
        }
        boolean changed = VillagerPoiAdapter.clearTransientProfession(data);
        VillagerCapturerItem.setCapturedVillagerData(items.get(VILLAGER_SLOT), data);
        if (changed) {
            selectedOfferIndex = 0;
            offerAgeTicks = 0;
            invalidateVillagerCache();
        }
    }

    private void prepareAutomaticPrices(Villager villager) {
        int careerDiscount = Math.max(0, villager.getVillagerData().level() - 1);
        int cureDiscount = Math.max(0, villager.getPersistentData().getInt(CURE_DISCOUNT_TAG).orElse(0));
        int discount = careerDiscount + cureDiscount;
        for (MerchantOffer offer : villager.getOffers()) {
            offer.resetSpecialPriceDiff();
            int applied = Math.min(discount, Math.max(0, offer.getBaseCostA().getCount() - 1));
            offer.addToSpecialPriceDiff(-applied);
        }
    }

    private void persistCachedVillager() {
        if (cachedVillager == null || level == null || level.isClientSide()) {
            return;
        }
        CompoundTag saved = VillagerCapturerItem.createCapturedVillagerData(cachedVillager);
        VillagerCapturerItem.setCapturedVillagerData(items.get(VILLAGER_SLOT), saved);
        cachedVillagerData = saved.copy();
        setChanged();
    }

    private void invalidateVillagerCache() {
        cachedVillager = null;
        cachedVillagerData = null;
    }

    private void tickOfferAge(@Nullable Villager villager) {
        if (villager == null || villager.getOffers().isEmpty()) {
            if (offerAgeTicks != 0) {
                offerAgeTicks = 0;
                setChanged();
            }
            return;
        }
        int maximumAge = VillagerOfferPersistence.refreshIntervalTicks();
        if (offerAgeTicks < maximumAge) {
            offerAgeTicks++;
            if (offerAgeTicks % 20 == 0 || offerAgeTicks == maximumAge) {
                setChanged();
            }
        }
    }

    private int countMatching(int[] slots, ItemCost cost) {
        int count = 0;
        for (int slot : slots) {
            ItemStack stack = items.get(slot);
            if (cost.test(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void consume(int[] slots, ItemCost cost, int count) {
        int remaining = count;
        for (int slot : slots) {
            ItemStack stack = items.get(slot);
            if (remaining > 0 && cost.test(stack)) {
                int consumed = Math.min(remaining, stack.getCount());
                stack.shrink(consumed);
                remaining -= consumed;
                if (stack.isEmpty()) {
                    items.set(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    private boolean canStoreOutput(ItemStack source) {
        int remaining = source.getCount();
        for (int slot : OUTPUT_SLOTS) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) {
                remaining -= source.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(stack, source)) {
                remaining -= stack.getMaxStackSize() - stack.getCount();
            }
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    private void storeOutput(ItemStack source) {
        ItemStack remaining = source.copy();
        for (int slot : OUTPUT_SLOTS) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, remaining)) {
                int moved = Math.min(remaining.getCount(), stack.getMaxStackSize() - stack.getCount());
                stack.grow(moved);
                remaining.shrink(moved);
            }
        }
        for (int slot : OUTPUT_SLOTS) {
            if (remaining.isEmpty()) {
                return;
            }
            if (items.get(slot).isEmpty()) {
                int moved = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                ItemStack inserted = remaining.copy();
                inserted.setCount(moved);
                items.set(slot, inserted);
                remaining.shrink(moved);
            }
        }
    }

    private static boolean isAdultVillager(ItemStack stack) {
        return CapturedMobStackAdapter.isFilledCapturer(IncubatorKind.VILLAGER, stack)
                && !CapturedMobStackAdapter.isBaby(IncubatorKind.VILLAGER, stack);
    }

    private static boolean isInputASlot(int slot) {
        return slot >= FIRST_INPUT_A_SLOT && slot < FIRST_INPUT_B_SLOT;
    }

    private static boolean isInputBSlot(int slot) {
        return slot >= FIRST_INPUT_B_SLOT && slot < FIRST_OUTPUT_SLOT;
    }

    private static boolean isOutputSlot(int slot) {
        return slot >= FIRST_OUTPUT_SLOT && slot < CONTAINER_SIZE;
    }

    private static boolean isValidSlot(int slot) {
        return slot >= 0 && slot < CONTAINER_SIZE;
    }

    private static final class AutotraderVillager extends Villager {
        private AutotraderVillager(net.minecraft.world.level.Level level) {
            super(VillagerCapturerItem.capturedVillagerType(), level);
        }

        private int completeAutomaticTrade(MerchantOffer offer) {
            offer.increaseUses();
            int minimumExperience = Math.min(
                    MachineSettings.values().autotraderMinimumExperience(),
                    MachineSettings.values().autotraderMaximumExperience()
            );
            int maximumExperience = Math.max(
                    MachineSettings.values().autotraderMinimumExperience(),
                    MachineSettings.values().autotraderMaximumExperience()
            );
            int rewardExperience = minimumExperience + getRandom().nextInt(maximumExperience - minimumExperience + 1);
            setVillagerXp(getVillagerXp() + offer.getXp());
            if (forceCareerLevelUpdate()) {
                rewardExperience += MachineSettings.values().autotraderLevelUpExperienceBonus();
            }
            return offer.shouldRewardExp() ? rewardExperience : 0;
        }

        private boolean forceCareerLevelUpdate() {
            if (!(level() instanceof ServerLevel serverLevel)) {
                return false;
            }
            boolean leveledUp = false;
            while (VillagerData.canLevelUp(getVillagerData().level())
                    && getVillagerXp() >= VillagerData.getMaxXpPerLevel(getVillagerData().level())) {
                setVillagerData(getVillagerData().withLevel(getVillagerData().level() + 1));
                updateTrades(serverLevel);
                leveledUp = true;
            }
            return leveledUp;
        }
    }
}
