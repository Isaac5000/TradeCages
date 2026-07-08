package com.example.examplemod.feature.tradecages.adapters.input;

import com.example.examplemod.feature.tradecages.adapters.output.TradingCellsRegistrationAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillagerTradingCellBlockEntity extends BlockEntity {
    private static final String LEGACY_VILLAGER_DATA_TAG = "StoredVillager";
    private static final String ENTITY_KIND_TAG = "StoredEntityKind";
    private static final String ENTITY_DATA_TAG = "StoredEntity";
    private static final String POI_STACK_TAG = "StoredPoi";

    private static final String VILLAGER_DATA_TAG = "VillagerData";
    private static final String PROFESSION_TAG = "profession";
    private static final String LEVEL_TAG = "level";
    private static final String XP_TAG = "Xp";
    private static final String AGE_TAG = "Age";
    private static final String NONE_PROFESSION = "minecraft:none";

    private static final Map<UUID, GlobalPos> OPEN_TRADING_CELLS_BY_PLAYER = new HashMap<>();

    private @Nullable StoredEntityKind storedEntityKind;
    private @Nullable CompoundTag storedEntityData;
    private ItemStack storedPoiStack = ItemStack.EMPTY;
    private @Nullable TradingCellVillager merchantVillager;

    public static void handleResetTradesRequest(Player player) {
        GlobalPos globalPos = OPEN_TRADING_CELLS_BY_PLAYER.get(player.getUUID());
        if (globalPos == null || player.level().getServer() == null) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.trades_reset_unavailable"));
            return;
        }

        Level level = player.level().getServer().getLevel(globalPos.dimension());
        if (level == null) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.trades_reset_unavailable"));
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(globalPos.pos());
        if (blockEntity instanceof VillagerTradingCellBlockEntity cell) {
            cell.resetTransientTrades(player);
        } else {
            OPEN_TRADING_CELLS_BY_PLAYER.remove(player.getUUID());
            player.sendSystemMessage(Component.translatable("message.trading_cells.trades_reset_unavailable"));
        }
    }

    public VillagerTradingCellBlockEntity(BlockPos pos, BlockState blockState) {
        super(TradingCellsRegistrationAdapter.VILLAGER_TRADING_CELL_BLOCK_ENTITY.get(), pos, blockState);
    }

    public boolean hasStoredEntity() {
        return storedEntityKind != null && storedEntityData != null && !storedEntityData.isEmpty();
    }

    public boolean hasVillager() {
        return hasStoredEntity() && storedEntityKind == StoredEntityKind.VILLAGER;
    }

    public boolean hasPiglin() {
        return hasStoredEntity() && storedEntityKind == StoredEntityKind.PIGLIN;
    }

    public boolean hasStoredPoi() {
        return !storedPoiStack.isEmpty();
    }

    public ItemStack copyPoiStack() {
        return storedPoiStack.copy();
    }

    public @Nullable CompoundTag copyVillagerData() {
        return hasVillager() && storedEntityData != null ? storedEntityData.copy() : null;
    }

    public @Nullable CompoundTag copyPiglinData() {
        return hasPiglin() && storedEntityData != null ? storedEntityData.copy() : null;
    }

    public @Nullable CompoundTag copyStoredEntityData() {
        return hasStoredEntity() && storedEntityData != null ? storedEntityData.copy() : null;
    }

    public @Nullable String getStoredEntityKindId() {
        return storedEntityKind == null ? null : storedEntityKind.id;
    }

    public @Nullable Entity createStoredEntityForDisplay() {
        if (level == null || !hasStoredEntity() || storedEntityData == null || storedEntityKind == null) {
            return null;
        }

        return switch (storedEntityKind) {
            case VILLAGER -> VillagerCapturerItem.createCapturedVillager(level, storedEntityData, worldPosition);
            case PIGLIN -> PiglinCapturerItem.createCapturedPiglin(level, storedEntityData, worldPosition);
        };
    }

    public InteractionResult insertVillagerFromCapturer(ItemStack stack, Player player) {
        if (hasStoredEntity()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.cell_occupied"));
            return InteractionResult.SUCCESS_SERVER;
        }

        CompoundTag villagerData = VillagerCapturerItem.getCapturedVillagerData(stack);
        if (villagerData == null) {
            return InteractionResult.PASS;
        }

        setStoredEntity(StoredEntityKind.VILLAGER, villagerData);
        refreshVillagerProfessionFromPoi();
        VillagerCapturerItem.clearCapturedVillager(stack);
        player.sendSystemMessage(Component.translatable("message.trading_cells.villager_inserted"));
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult insertPiglinFromCapturer(ItemStack stack, Player player) {
        if (hasStoredEntity()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.cell_occupied"));
            return InteractionResult.SUCCESS_SERVER;
        }

        CompoundTag piglinData = PiglinCapturerItem.getCapturedPiglinData(stack);
        if (piglinData == null) {
            return InteractionResult.PASS;
        }

        setStoredEntity(StoredEntityKind.PIGLIN, piglinData);
        PiglinCapturerItem.clearCapturedPiglin(stack);
        player.sendSystemMessage(Component.translatable("message.trading_cells.piglin_inserted"));
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult extractVillagerToCapturer(ItemStack stack, Player player) {
        if (!hasStoredEntity()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.cell_empty"));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!hasVillager()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.wrong_capturer"));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (VillagerCapturerItem.hasCapturedVillager(stack)) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.capturer_occupied"));
            return InteractionResult.SUCCESS_SERVER;
        }

        saveProxyToStoredData();
        CompoundTag villagerData = copyVillagerData();
        if (villagerData == null) {
            return InteractionResult.FAIL;
        }

        VillagerCapturerItem.setCapturedVillagerData(stack, villagerData);
        clearStoredEntity();
        player.sendSystemMessage(Component.translatable("message.trading_cells.villager_extracted"));
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult extractPiglinToCapturer(ItemStack stack, Player player) {
        if (!hasStoredEntity()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.cell_empty"));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!hasPiglin()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.wrong_capturer"));
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
        clearStoredEntity();
        player.sendSystemMessage(Component.translatable("message.trading_cells.piglin_extracted"));
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult insertPoiFromStack(ItemStack stack, Player player) {
        String professionId = getProfessionForPoiStack(stack);
        if (professionId == null) {
            return InteractionResult.PASS;
        }

        boolean replacingPoi = hasStoredPoi();
        if (replacingPoi) {
            saveProxyToStoredData();
            if (hasPersistentVillagerProfession()) {
                player.sendSystemMessage(Component.translatable("message.trading_cells.poi_locked_by_profession"));
                return InteractionResult.SUCCESS_SERVER;
            }

            ItemStack previousPoi = storedPoiStack.copy();
            if (!player.getInventory().add(previousPoi)) {
                player.sendSystemMessage(Component.translatable("message.trading_cells.inventory_full"));
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        storedPoiStack = stack.copyWithCount(1);
        stack.shrink(1);
        refreshVillagerProfessionFromPoi();
        markChangedAndSync();
        player.sendSystemMessage(Component.translatable(
                replacingPoi ? "message.trading_cells.poi_replaced" : "message.trading_cells.poi_inserted",
                storedPoiStack.getHoverName()
        ));
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult extractPoiToPlayer(Player player) {
        if (!hasStoredPoi()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.poi_empty"));
            return InteractionResult.SUCCESS_SERVER;
        }

        ItemStack poiToReturn = storedPoiStack.copy();
        if (!player.getInventory().add(poiToReturn)) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.inventory_full"));
            return InteractionResult.SUCCESS_SERVER;
        }

        storedPoiStack = ItemStack.EMPTY;
        clearTransientVillagerProfession();
        markChangedAndSync();
        player.sendSystemMessage(Component.translatable("message.trading_cells.poi_extracted"));
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult releaseStoredEntityIntoWorld(Player player, BlockPos releasePos) {
        if (!hasStoredEntity()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.cell_empty"));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (level == null || level.isClientSide() || storedEntityKind == null || storedEntityData == null) {
            return InteractionResult.SUCCESS;
        }

        saveProxyToStoredData();
        Entity entity = createEntity(level, storedEntityKind, storedEntityData, releasePos);
        if (entity == null || !level.addFreshEntity(entity)) {
            return InteractionResult.FAIL;
        }

        StoredEntityKind releasedKind = storedEntityKind;
        clearStoredEntity();
        player.sendSystemMessage(Component.translatable(releasedKind.releaseMessageKey));
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult resetTransientTrades(Player player) {
        if (!hasVillager()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.trades_reset_unavailable"));
            return InteractionResult.SUCCESS_SERVER;
        }

        saveProxyToStoredData();
        if (storedEntityData == null) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.trades_reset_unavailable"));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (isVillagerBaby(storedEntityData)) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.baby_cannot_trade"));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (isVillagerProfessionPersistent(storedEntityData)) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.trades_reset_locked"));
            return InteractionResult.SUCCESS_SERVER;
        }

        String professionId = getProfessionForPoiStack(storedPoiStack);
        if (professionId == null) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.poi_empty"));
            return InteractionResult.SUCCESS_SERVER;
        }

        TradingCellVillager villager = getOrCreateMerchantVillager();
        if (villager == null) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.invalid_villager"));
            return InteractionResult.FAIL;
        }

        clearVillagerProfession(storedEntityData);
        setVillagerProfession(storedEntityData, professionId);
        reloadMerchantVillagerFromStoredData(villager);
        villager.setTradingPlayer(player);

        MerchantOffers offers = villager.getOffers();
        if (offers.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.no_trades"));
            return InteractionResult.SUCCESS_SERVER;
        }

        saveProxyToStoredData();
        markChangedAndSync();
        player.sendMerchantOffers(
                player.containerMenu.containerId,
                offers,
                villager.getVillagerData().level(),
                villager.getVillagerXp(),
                villager.showProgressBar(),
                villager.canRestock()
        );
        player.sendSystemMessage(Component.translatable("message.trading_cells.trades_reset"));
        return InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult openTrade(Player player) {
        if (!hasStoredEntity()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.cell_empty"));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!hasVillager()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.piglin_no_trades"));
            return InteractionResult.SUCCESS_SERVER;
        }

        refreshVillagerProfessionFromPoi();
        TradingCellVillager villager = getOrCreateMerchantVillager();
        if (villager == null) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.invalid_villager"));
            return InteractionResult.FAIL;
        }

        if (villager.isBaby()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.baby_cannot_trade"));
            return InteractionResult.SUCCESS_SERVER;
        }

        MerchantOffers offers = villager.getOffers();
        if (offers.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.no_trades"));
            return InteractionResult.SUCCESS_SERVER;
        }

        registerTradingPlayer(player);
        villager.setTradingPlayer(player);
        villager.openTradingScreen(player, villager.getDisplayName(), villager.getVillagerData().level());
        return InteractionResult.SUCCESS_SERVER;
    }

    public void dropStoredContents(Level level, BlockPos pos) {
        dropStoredEntityCapturer(level, pos);
        if (hasStoredPoi()) {
            Block.popResource(level, pos, storedPoiStack.copy());
            storedPoiStack = ItemStack.EMPTY;
            markChangedAndSync();
        }
    }

    public void dropStoredEntityCapturer(Level level, BlockPos pos) {
        if (!hasStoredEntity() || storedEntityKind == null) {
            return;
        }

        saveProxyToStoredData();
        CompoundTag entityData = copyStoredEntityData();
        if (entityData == null) {
            return;
        }

        ItemStack drop = switch (storedEntityKind) {
            case VILLAGER -> {
                ItemStack stack = new ItemStack(TradingCellsRegistrationAdapter.VILLAGER_CAPTURER_ITEM.get());
                VillagerCapturerItem.setCapturedVillagerData(stack, entityData);
                yield stack;
            }
            case PIGLIN -> {
                ItemStack stack = new ItemStack(TradingCellsRegistrationAdapter.PIGLIN_CAPTURER_ITEM.get());
                PiglinCapturerItem.setCapturedPiglinData(stack, entityData);
                yield stack;
            }
        };

        Block.popResource(level, pos, drop);
        clearStoredEntity();
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        storedEntityKind = input.getString(ENTITY_KIND_TAG).map(StoredEntityKind::fromId).orElse(null);
        storedEntityData = input.read(ENTITY_DATA_TAG, CompoundTag.CODEC).orElse(null);
        storedPoiStack = input.read(POI_STACK_TAG, ItemStack.CODEC).orElse(ItemStack.EMPTY);

        // Backwards compatibility with saves made by the earlier villager-only version.
        if (storedEntityData == null) {
            storedEntityData = input.read(LEGACY_VILLAGER_DATA_TAG, CompoundTag.CODEC).orElse(null);
            if (storedEntityData != null && !storedEntityData.isEmpty()) {
                storedEntityKind = StoredEntityKind.VILLAGER;
            }
        }

        if (storedEntityKind == null || storedEntityData == null || storedEntityData.isEmpty()) {
            storedEntityKind = null;
            storedEntityData = null;
        }
        if (storedPoiStack.isEmpty()) {
            storedPoiStack = ItemStack.EMPTY;
        }

        merchantVillager = null;
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        saveProxyToStoredData();
        super.saveAdditional(output);
        if (storedEntityKind != null && storedEntityData != null && !storedEntityData.isEmpty()) {
            output.putString(ENTITY_KIND_TAG, storedEntityKind.id);
            output.store(ENTITY_DATA_TAG, CompoundTag.CODEC, storedEntityData);
        }
        if (!storedPoiStack.isEmpty()) {
            output.store(POI_STACK_TAG, ItemStack.CODEC, storedPoiStack);
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

    private void setStoredEntity(StoredEntityKind kind, CompoundTag entityData) {
        storedEntityKind = kind;
        storedEntityData = entityData.copy();
        merchantVillager = null;
        markChangedAndSync();
    }

    private @Nullable TradingCellVillager getOrCreateMerchantVillager() {
        if (merchantVillager != null) {
            return merchantVillager;
        }

        if (level == null || !hasVillager() || storedEntityData == null) {
            return null;
        }

        TradingCellVillager villager = new TradingCellVillager(level, this);
        villager.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), storedEntityData.copy()));
        villager.setPos(worldPosition.getX() + 0.5D, worldPosition.getY(), worldPosition.getZ() + 0.5D);
        villager.setPersistenceRequired();
        merchantVillager = villager;
        return merchantVillager;
    }

    private void reloadMerchantVillagerFromStoredData(TradingCellVillager villager) {
        if (level == null || storedEntityData == null) {
            return;
        }
        villager.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), storedEntityData.copy()));
        villager.setPos(worldPosition.getX() + 0.5D, worldPosition.getY(), worldPosition.getZ() + 0.5D);
        villager.setPersistenceRequired();
        merchantVillager = villager;
    }

    private static @Nullable Entity createEntity(Level level, StoredEntityKind kind, CompoundTag entityData, BlockPos position) {
        return switch (kind) {
            case VILLAGER -> VillagerCapturerItem.createCapturedVillager(level, entityData, position);
            case PIGLIN -> PiglinCapturerItem.createCapturedPiglin(level, entityData, position);
        };
    }

    private void saveProxyToStoredData() {
        if (merchantVillager != null && storedEntityKind == StoredEntityKind.VILLAGER) {
            storedEntityData = VillagerCapturerItem.createCapturedVillagerData(merchantVillager);
        }
    }

    private void refreshVillagerProfessionFromPoi() {
        if (!hasVillager() || storedEntityData == null) {
            return;
        }

        saveProxyToStoredData();
        if (isVillagerProfessionPersistent(storedEntityData)) {
            return;
        }
        if (isVillagerBaby(storedEntityData)) {
            clearVillagerProfession(storedEntityData);
            merchantVillager = null;
            return;
        }

        String professionId = getProfessionForPoiStack(storedPoiStack);
        if (professionId == null) {
            clearVillagerProfession(storedEntityData);
        } else {
            setVillagerProfession(storedEntityData, professionId);
        }
        merchantVillager = null;
    }

    private void clearTransientVillagerProfession() {
        if (!hasVillager() || storedEntityData == null) {
            return;
        }

        saveProxyToStoredData();
        if (!isVillagerProfessionPersistent(storedEntityData)) {
            clearVillagerProfession(storedEntityData);
            merchantVillager = null;
        }
    }

    private boolean hasPersistentVillagerProfession() {
        return hasVillager() && storedEntityData != null && isVillagerProfessionPersistent(storedEntityData);
    }

    private static boolean isVillagerProfessionPersistent(CompoundTag villagerData) {
        if (villagerData.getInt(XP_TAG).orElse(0) > 0) {
            return true;
        }

        return villagerData.getCompound(VILLAGER_DATA_TAG)
                .flatMap(data -> data.getInt(LEVEL_TAG))
                .map(level -> level > 1)
                .orElse(false);
    }

    private static boolean isVillagerBaby(CompoundTag villagerData) {
        return villagerData.getInt(AGE_TAG).orElse(0) < 0;
    }

    private static void setVillagerProfession(CompoundTag villagerData, String professionId) {
        CompoundTag data = villagerData.getCompound(VILLAGER_DATA_TAG).map(CompoundTag::copy).orElseGet(CompoundTag::new);
        data.putString(PROFESSION_TAG, professionId);
        if (data.getInt(LEVEL_TAG).isEmpty()) {
            data.putInt(LEVEL_TAG, 1);
        }
        villagerData.put(VILLAGER_DATA_TAG, data);
        villagerData.remove("Offers");
    }

    private static void clearVillagerProfession(CompoundTag villagerData) {
        setVillagerProfession(villagerData, NONE_PROFESSION);
        villagerData.putInt(XP_TAG, 0);
    }

    private static @Nullable String getProfessionForPoiStack(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        return getProfessionForPoiBlock(blockItem.getBlock());
    }

    private static @Nullable String getProfessionForPoiBlock(Block block) {
        if (block == Blocks.BARREL) return "minecraft:fisherman";
        if (block == Blocks.BLAST_FURNACE) return "minecraft:armorer";
        if (block == Blocks.BREWING_STAND) return "minecraft:cleric";
        if (block == Blocks.CARTOGRAPHY_TABLE) return "minecraft:cartographer";
        if (block == Blocks.CAULDRON) return "minecraft:leatherworker";
        if (block == Blocks.COMPOSTER) return "minecraft:farmer";
        if (block == Blocks.FLETCHING_TABLE) return "minecraft:fletcher";
        if (block == Blocks.GRINDSTONE) return "minecraft:weaponsmith";
        if (block == Blocks.LECTERN) return "minecraft:librarian";
        if (block == Blocks.LOOM) return "minecraft:shepherd";
        if (block == Blocks.SMITHING_TABLE) return "minecraft:toolsmith";
        if (block == Blocks.SMOKER) return "minecraft:butcher";
        if (block == Blocks.STONECUTTER) return "minecraft:mason";
        return null;
    }

    private void registerTradingPlayer(Player player) {
        if (level != null) {
            OPEN_TRADING_CELLS_BY_PLAYER.put(player.getUUID(), GlobalPos.of(level.dimension(), worldPosition));
        }
    }

    private static void unregisterTradingPlayer(UUID playerId) {
        OPEN_TRADING_CELLS_BY_PLAYER.remove(playerId);
    }

    private void ownerSafeSave(TradingCellVillager villager) {
        merchantVillager = villager;
        saveProxyToStoredData();
        markChangedAndSync();
    }

    private void clearStoredEntity() {
        storedEntityKind = null;
        storedEntityData = null;
        merchantVillager = null;
        markChangedAndSync();
    }

    private void markChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private boolean isPlayerStillValid(Player player) {
        return !isRemoved()
                && level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D
                ) <= 64.0D;
    }

    private enum StoredEntityKind {
        VILLAGER("villager", "message.trading_cells.villager_released"),
        PIGLIN("piglin", "message.trading_cells.piglin_released");

        private final String id;
        private final String releaseMessageKey;

        StoredEntityKind(String id, String releaseMessageKey) {
            this.id = id;
            this.releaseMessageKey = releaseMessageKey;
        }

        private static @Nullable StoredEntityKind fromId(String id) {
            for (StoredEntityKind kind : values()) {
                if (kind.id.equals(id)) {
                    return kind;
                }
            }
            return null;
        }
    }

    private static final class TradingCellVillager extends Villager {
        private final VillagerTradingCellBlockEntity owner;
        private @Nullable UUID currentTradingPlayerId;

        private TradingCellVillager(Level level, VillagerTradingCellBlockEntity owner) {
            super(CapturedEntityTypes.villagerType(), level);
            this.owner = owner;
        }

        @Override
        public void setTradingPlayer(@Nullable Player player) {
            super.setTradingPlayer(player);
            if (player == null) {
                if (currentTradingPlayerId != null) {
                    unregisterTradingPlayer(currentTradingPlayerId);
                    currentTradingPlayerId = null;
                }
                owner.saveProxyToStoredData();
                owner.markChangedAndSync();
            } else {
                currentTradingPlayerId = player.getUUID();
                owner.registerTradingPlayer(player);
            }
        }

        @Override
        public void notifyTrade(@NonNull MerchantOffer offer) {
            super.notifyTrade(offer);
            forceCareerLevelUpdate();
            owner.saveProxyToStoredData();
            owner.markChangedAndSync();
        }

        private void forceCareerLevelUpdate() {
            if (!(level() instanceof ServerLevel serverLevel)) {
                return;
            }

            while (VillagerData.canLevelUp(getVillagerData().level())
                    && getVillagerXp() >= VillagerData.getMaxXpPerLevel(getVillagerData().level())) {
                setVillagerData(getVillagerData().withLevel(getVillagerData().level() + 1));
                updateTrades(serverLevel);
            }
        }

        @Override
        public void notifyTradeUpdated(@NonNull ItemStack stack) {
            super.notifyTradeUpdated(stack);
            owner.saveProxyToStoredData();
            owner.markChangedAndSync();
        }

        @Override
        public boolean stillValid(@NonNull Player player) {
            return owner.isPlayerStillValid(player);
        }
    }
}
