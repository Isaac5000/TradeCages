package com.cosmocraft.trading_cells.feature.tradecages.adapters.input;

import com.cosmocraft.trading_cells.feature.tradecages.adapters.output.TradingCellsRegistrationAdapter;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.stats.Stats;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class VillagerTradingCellBlock extends BaseEntityBlock {
    public static final MapCodec<VillagerTradingCellBlock> CODEC = simpleCodec(VillagerTradingCellBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public VillagerTradingCellBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 0;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new VillagerTradingCellBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            @NonNull Level level,
            @NonNull BlockState state,
            @NonNull BlockEntityType<T> type
    ) {
        return level.isClientSide() ? null : createTickerHelper(
                type,
                TradingCellsRegistrationAdapter.VILLAGER_TRADING_CELL_BLOCK_ENTITY.get(),
                VillagerTradingCellBlockEntity::serverTick
        );
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NonNull InteractionResult useItemOn(
            @NonNull ItemStack stack,
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull InteractionHand hand,
            @NonNull BlockHitResult hit
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        VillagerTradingCellBlockEntity cell = getCell(level, pos);
        if (cell == null) {
            return InteractionResult.FAIL;
        }

        if (stack.getItem() instanceof VillagerCapturerItem) {
            if (VillagerCapturerItem.hasCapturedVillager(stack)) {
                return cell.insertVillagerFromCapturer(stack, player);
            }

            return cell.extractVillagerToCapturer(stack, player);
        }

        if (stack.getItem() instanceof PiglinCapturerItem) {
            if (cell.hasPiglin() && !PiglinCapturerItem.hasCapturedPiglin(stack)) {
                return cell.extractPiglinToCapturer(stack, player);
            }
            return InteractionResult.SUCCESS_SERVER;
        }

        if (player.isShiftKeyDown()) {
            return cell.extractPoiToPlayer(player);
        }

        if (stack.getItem() instanceof BlockItem) {
            InteractionResult result = cell.insertPoiFromStack(stack, player);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }

        return cell.openTrade(player);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull BlockHitResult hit
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        VillagerTradingCellBlockEntity cell = getCell(level, pos);
        if (cell == null) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            return cell.extractPoiToPlayer(player);
        }

        return cell.openTrade(player);
    }

    @Override
    public void playerDestroy(
            @NonNull Level level,
            @NonNull Player player,
            @NonNull BlockPos pos,
            @NonNull BlockState state,
            @Nullable BlockEntity blockEntity,
            @NonNull ItemStack tool
    ) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        if (level.isClientSide() || player.isCreative() || !(blockEntity instanceof VillagerTradingCellBlockEntity cell)) {
            return;
        }

        ItemStack drop = new ItemStack(TradingCellsRegistrationAdapter.TRADE_CAGE_ITEM.get());
        CompoundTag data = cell.saveCustomOnly(level.registryAccess());
        if (!data.isEmpty()) {
            drop.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(cell.getType(), data));
        }
        Block.popResource(level, pos, drop);
        cell.discardContentsAfterBlockDrop();
    }

    private static @Nullable VillagerTradingCellBlockEntity getCell(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof VillagerTradingCellBlockEntity cell) {
            return cell;
        }
        return null;
    }
}
