package com.example.examplemod.feature.breeders.adapters.input;

import com.example.examplemod.feature.breeders.domain.model.BreederKind;
import com.example.examplemod.feature.breeders.domain.model.BreederRecipe;
import com.example.examplemod.feature.breeders.adapters.output.BreederRegistrationAdapter;
import com.example.examplemod.feature.tradecages.adapters.input.PiglinCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.output.TradingCellsRegistrationAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class BreederBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int FOOD_SLOT = 0;
    public static final int PARENT_A_SLOT = 1;
    public static final int PARENT_B_SLOT = 2;
    public static final int BABY_PREVIEW_SLOT = 3;
    public static final int EMPTY_CAPTURER_SLOT = 4;
    public static final int FILLED_CAPTURER_SLOT = 5;
    public static final int CONTAINER_SIZE = 6;

    private static final String SLOT_TAG_PREFIX = "Slot";
    private static final String BREED_TICKS_TAG = "BreedTicks";
    private static final String PENDING_BABIES_TAG = "PendingBabies";
    private static final String BABY_TEMPLATE_TAG = "BabyTemplate";
    private static final int MAX_PENDING_BABIES = 64;

    private static final int[] TOP_SLOTS = new int[]{FOOD_SLOT};
    private static final int[] SIDE_SLOTS = new int[]{PARENT_A_SLOT, PARENT_B_SLOT, EMPTY_CAPTURER_SLOT};
    private static final int[] BOTTOM_SLOTS = new int[]{FILLED_CAPTURER_SLOT};
    private static final int[] NO_SLOTS = new int[0];

    private final BreederKind kind;
    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int breedTicks;
    private int pendingBabies;
    private @Nullable CompoundTag babyTemplate;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> breedTicks;
                case 1 -> pendingBabies;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                breedTicks = value;
            } else if (index == 1) {
                pendingBabies = Math.max(0, value);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    protected BreederBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, BreederKind kind) {
        super(type, pos, blockState);
        this.kind = kind;
    }

    public BreederKind kind() {
        return kind;
    }

    public int getBreedTicks() {
        return breedTicks;
    }

    public int getPendingBabies() {
        return pendingBabies;
    }

    public ContainerData dataAccess() {
        return dataAccess;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BreederBlockEntity breeder) {
        if (level.isClientSide()) {
            return;
        }
        BreederRegistrationAdapter.TICK_BREEDER_USE_CASE.tick(breeder);
    }

    public void processAutomation() {
        if (pendingBabies <= 0 || !getItem(FILLED_CAPTURER_SLOT).isEmpty()) {
            return;
        }

        ItemStack capturers = getItem(EMPTY_CAPTURER_SLOT);
        if (!isEmptyCapturer(capturers)) {
            return;
        }

        CompoundTag babyData = createBabyDataForOutput();
        if (babyData == null) {
            return;
        }

        ItemStack output = new ItemStack(capturerItem());
        setCapturedData(output, babyData);
        capturers.shrink(1);
        if (capturers.isEmpty()) {
            items.set(EMPTY_CAPTURER_SLOT, ItemStack.EMPTY);
        }
        items.set(FILLED_CAPTURER_SLOT, output);
        pendingBabies--;
        if (pendingBabies <= 0) {
            pendingBabies = 0;
        }
        markChangedAndSync();
    }

    public void processBreedingProgress() {
        if (!canGenerateBaby()) {
            if (breedTicks != 0) {
                breedTicks = 0;
                markChangedAndSync();
            }
            return;
        }

        breedTicks++;
        if (breedTicks < BreederRecipe.breedTicks(kind)) {
            setChanged();
            return;
        }

        breedTicks = 0;
        consumeFoodCost();
        babyTemplate = createBabyTemplateFromParents();
        pendingBabies = Math.min(MAX_PENDING_BABIES, pendingBabies + 1);
        markChangedAndSync();
    }

    private boolean canGenerateBaby() {
        return pendingBabies < MAX_PENDING_BABIES
                && isValidAdultParent(getItem(PARENT_A_SLOT))
                && isValidAdultParent(getItem(PARENT_B_SLOT))
                && hasFoodCost();
    }

    private boolean hasFoodCost() {
        ItemStack food = getItem(FOOD_SLOT);
        return BreederRecipe.isFood(kind, food) && food.getCount() >= BreederRecipe.cost(kind, food);
    }

    private void consumeFoodCost() {
        ItemStack food = getItem(FOOD_SLOT);
        if (!food.isEmpty()) {
            food.shrink(BreederRecipe.cost(kind, food));
            if (food.isEmpty()) {
                items.set(FOOD_SLOT, ItemStack.EMPTY);
            }
        }
    }

    private @Nullable CompoundTag createBabyDataForOutput() {
        if (babyTemplate != null && !babyTemplate.isEmpty()) {
            return makeBabyData(babyTemplate);
        }
        return createBabyTemplateFromParents();
    }

    private @Nullable CompoundTag createBabyTemplateFromParents() {
        CompoundTag parentData = getParentData(getItem(PARENT_A_SLOT));
        if (parentData == null) {
            parentData = getParentData(getItem(PARENT_B_SLOT));
        }
        if (parentData == null) {
            return null;
        }
        return makeBabyData(parentData);
    }

    private CompoundTag makeBabyData(CompoundTag source) {
        CompoundTag baby = source.copy();
        baby.remove("UUID");
        baby.remove("CustomName");
        baby.remove("LoveCause");
        baby.remove("LoveCauseLeast");
        baby.remove("LoveCauseMost");
        baby.remove("AgeLocked");
        baby.putInt("Age", -24000);
        if (kind == BreederKind.VILLAGER) {
            CompoundTag villagerData = baby.getCompound("VillagerData").map(CompoundTag::copy).orElseGet(CompoundTag::new);
            villagerData.putString("type", getBiomeVillagerTypeId());
            baby.put("VillagerData", villagerData);
        } else {
            baby.putBoolean("IsBaby", true);
        }
        return baby;
    }

    private String getBiomeVillagerTypeId() {
        if (level == null) {
            return "minecraft:plains";
        }
        try {
            ResourceKey<VillagerType> villagerType = VillagerType.byBiome(level.getBiome(worldPosition));
            return villagerType.identifier().toString();
        } catch (Exception ignored) {
            return "minecraft:plains";
        }
    }

    public ItemStack createBabyPreviewStack() {
        if (pendingBabies <= 0) {
            return ItemStack.EMPTY;
        }
        CompoundTag babyData = createBabyDataForOutput();
        if (babyData == null) {
            return ItemStack.EMPTY;
        }
        ItemStack preview = new ItemStack(capturerItem());
        setCapturedData(preview, babyData);
        return preview;
    }

    private @Nullable CompoundTag getParentData(ItemStack stack) {
        if (kind == BreederKind.VILLAGER) {
            return VillagerCapturerItem.getCapturedVillagerData(stack);
        }
        return PiglinCapturerItem.getCapturedPiglinData(stack);
    }

    private boolean isValidAdultParent(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(capturerItem())) {
            return false;
        }
        CompoundTag data = getParentData(stack);
        if (data == null) {
            return false;
        }
        return kind == BreederKind.VILLAGER
                ? !VillagerCapturerItem.isBabyVillager(data)
                : !PiglinCapturerItem.isBabyPiglin(data);
    }

    private boolean isEmptyCapturer(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(capturerItem())) {
            return false;
        }
        return kind == BreederKind.VILLAGER
                ? !VillagerCapturerItem.hasCapturedVillager(stack)
                : !PiglinCapturerItem.hasCapturedPiglin(stack);
    }

    private net.minecraft.world.item.Item capturerItem() {
        return kind == BreederKind.VILLAGER
                ? TradingCellsRegistrationAdapter.VILLAGER_CAPTURER_ITEM.get()
                : TradingCellsRegistrationAdapter.PIGLIN_CAPTURER_ITEM.get();
    }

    private void setCapturedData(ItemStack stack, CompoundTag data) {
        if (kind == BreederKind.VILLAGER) {
            VillagerCapturerItem.setCapturedVillagerData(stack, data);
        } else {
            PiglinCapturerItem.setCapturedPiglinData(stack, data);
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            if (slot == BABY_PREVIEW_SLOT) {
                continue;
            }
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level, pos, stack.copy());
                items.set(slot, ItemStack.EMPTY);
            }
        }
        while (pendingBabies > 0) {
            CompoundTag babyData = createBabyDataForOutput();
            if (babyData == null) {
                break;
            }
            ItemStack stack = new ItemStack(capturerItem());
            setCapturedData(stack, babyData);
            Block.popResource(level, pos, stack);
            pendingBabies--;
        }
        markChangedAndSync();
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable(kind == BreederKind.VILLAGER
                ? "container.trading_cells.villager_breeder"
                : "container.trading_cells.piglin_breeder");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NonNull Inventory inventory, @NonNull Player player) {
        return new BreederMenu(kind, containerId, inventory, this, dataAccess);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        if (pendingBabies > 0) {
            return false;
        }
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            if (i != BABY_PREVIEW_SLOT && !items.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        if (slot == BABY_PREVIEW_SLOT) {
            return createBabyPreviewStack();
        }
        return isValidSlot(slot) ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int count) {
        if (!isValidSlot(slot) || slot == BABY_PREVIEW_SLOT || count <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = stack.split(count);
        if (stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
        markChangedAndSync();
        return removed;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        if (!isValidSlot(slot) || slot == BABY_PREVIEW_SLOT) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return removed;
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack stack) {
        if (!isValidSlot(slot) || slot == BABY_PREVIEW_SLOT) {
            return;
        }
        if (!stack.isEmpty() && !canPlaceItem(slot, stack)) {
            return;
        }
        items.set(slot, stack.copy());
        if (!items.get(slot).isEmpty() && items.get(slot).getCount() > getMaxStackSize()) {
            items.get(slot).setCount(getMaxStackSize());
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
            case FOOD_SLOT -> BreederRecipe.isFood(kind, stack);
            case PARENT_A_SLOT, PARENT_B_SLOT -> isValidAdultParent(stack);
            case EMPTY_CAPTURER_SLOT -> isEmptyCapturer(stack);
            default -> false;
        };
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            if (slot != BABY_PREVIEW_SLOT) {
                items.set(slot, ItemStack.EMPTY);
            }
        }
        breedTicks = 0;
        pendingBabies = 0;
        babyTemplate = null;
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
        if (direction == Direction.UP) {
            return slot == FOOD_SLOT && canPlaceItem(slot, stack);
        }
        if (direction != null && direction.getAxis().isHorizontal()) {
            return (slot == PARENT_A_SLOT || slot == PARENT_B_SLOT || slot == EMPTY_CAPTURER_SLOT) && canPlaceItem(slot, stack);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NonNull ItemStack stack, @NonNull Direction direction) {
        return direction == Direction.DOWN && slot == FILLED_CAPTURER_SLOT;
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            if (slot == BABY_PREVIEW_SLOT) {
                items.set(slot, ItemStack.EMPTY);
            } else {
                items.set(slot, input.read(SLOT_TAG_PREFIX + slot, ItemStack.CODEC).orElse(ItemStack.EMPTY));
            }
        }
        breedTicks = Math.max(0, input.getIntOr(BREED_TICKS_TAG, 0));
        pendingBabies = Math.max(0, input.getIntOr(PENDING_BABIES_TAG, 0));
        babyTemplate = input.read(BABY_TEMPLATE_TAG, CompoundTag.CODEC).orElse(null);
        if (babyTemplate != null && babyTemplate.isEmpty()) {
            babyTemplate = null;
        }
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        for (int slot = 0; slot < CONTAINER_SIZE; slot++) {
            if (slot != BABY_PREVIEW_SLOT && !items.get(slot).isEmpty()) {
                output.store(SLOT_TAG_PREFIX + slot, ItemStack.CODEC, items.get(slot));
            }
        }
        if (breedTicks > 0) {
            output.putInt(BREED_TICKS_TAG, breedTicks);
        }
        if (pendingBabies > 0) {
            output.putInt(PENDING_BABIES_TAG, pendingBabies);
        }
        if (babyTemplate != null && !babyTemplate.isEmpty()) {
            output.store(BABY_TEMPLATE_TAG, CompoundTag.CODEC, babyTemplate);
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

    protected void markChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static boolean isValidSlot(int slot) {
        return slot >= 0 && slot < CONTAINER_SIZE;
    }
}
