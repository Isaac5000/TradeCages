package com.cosmocraft.trading_cells.feature.incubators.adapters.input;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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

public abstract class IncubatorBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected IncubatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
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
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
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
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
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
        return openMenu(level, pos, player);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull BlockHitResult hit
    ) {
        return openMenu(level, pos, player);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            @NonNull Level level,
            @NonNull BlockState state,
            @NonNull BlockEntityType<T> type
    ) {
        if (level.isClientSide() || type != getBlockEntityType()) {
            return null;
        }
        return (tickerLevel, tickerPos, tickerState, tickerBlockEntity) ->
                IncubatorBlockEntity.serverTick(
                        tickerLevel,
                        tickerPos,
                        tickerState,
                        (IncubatorBlockEntity) tickerBlockEntity
                );
    }

    @Override
    public @NonNull BlockState playerWillDestroy(
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull BlockState state,
            @NonNull Player player
    ) {
        if (!level.isClientSide() && !player.isCreative()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof IncubatorBlockEntity incubator) {
                incubator.prepareForBlockDrop(level.registryAccess());
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
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
        if (level.isClientSide() || player.isCreative() || !(blockEntity instanceof IncubatorBlockEntity incubator)) {
            return;
        }

        ItemStack drop = new ItemStack(asItem());
        CompoundTag data = incubator.getPreparedBlockDropData(level.registryAccess());
        if (!data.isEmpty()) {
            drop.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(incubator.getType(), data));
        }
        Block.popResource(level, pos, drop);
        incubator.discardContentsAfterBlockDrop();
    }

    protected abstract BlockEntityType<? extends IncubatorBlockEntity> getBlockEntityType();

    private static InteractionResult openMenu(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IncubatorBlockEntity incubator && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(incubator);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.FAIL;
    }
}
