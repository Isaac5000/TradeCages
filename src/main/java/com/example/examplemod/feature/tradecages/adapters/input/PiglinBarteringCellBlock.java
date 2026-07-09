package com.example.examplemod.feature.tradecages.adapters.input;

import com.example.examplemod.feature.tradecages.adapters.output.TradingCellsRegistrationAdapter;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.stats.Stats;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class PiglinBarteringCellBlock extends BaseEntityBlock {
    public static final MapCodec<PiglinBarteringCellBlock> CODEC = simpleCodec(PiglinBarteringCellBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public PiglinBarteringCellBlock(Properties properties) {
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

    public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return 0; // allow light to pass through
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new PiglinBarteringCellBlockEntity(pos, state);
    }

    @Override
    protected @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            @NonNull Level level,
            @NonNull BlockState state,
            @NonNull BlockEntityType<T> type
    ) {
        return level.isClientSide() ? null : createTickerHelper(
                type,
                TradingCellsRegistrationAdapter.PIGLIN_BARTERING_CELL_BLOCK_ENTITY.get(),
                PiglinBarteringCellBlockEntity::serverTick
        );
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

        PiglinBarteringCellBlockEntity cell = getCell(level, pos);
        if (cell == null) {
            return InteractionResult.FAIL;
        }

        if (stack.getItem() instanceof PiglinCapturerItem) {
            if (PiglinCapturerItem.hasCapturedPiglin(stack)) {
                return cell.insertPiglinFromCapturer(stack, player);
            }
            return cell.extractPiglinToCapturer(stack, player);
        }

        if (stack.is(Items.GOLD_INGOT)) {
            return cell.barterGold(stack, player);
        }

        if (stack.getItem() instanceof VillagerCapturerItem) {
            player.sendSystemMessage(Component.translatable("message.trading_cells.wrong_capturer"));
            return InteractionResult.SUCCESS_SERVER;
        }

        return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
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

        PiglinBarteringCellBlockEntity cell = getCell(level, pos);
        if (cell == null) {
            return InteractionResult.FAIL;
        }

        if (cell.hasOutput()) {
            return cell.extractOutputToPlayer(player);
        }

        player.sendSystemMessage(Component.translatable(cell.hasPiglin()
                ? "message.trading_cells.piglin_barter_hint"
                : "message.trading_cells.cell_empty"));
        return InteractionResult.SUCCESS_SERVER;
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

        if (!level.isClientSide() && !player.isCreative() && blockEntity instanceof PiglinBarteringCellBlockEntity cell) {
            ItemStack drop = new ItemStack(TradingCellsRegistrationAdapter.PIGLIN_BARTERING_CELL_ITEM.get());
            CompoundTag data = cell.saveCustomOnly(level.registryAccess());
            if (!data.isEmpty()) {
                drop.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(cell.getType(), data));
            }
            Block.popResource(level, pos, drop);
        }
    }

    private static @Nullable PiglinBarteringCellBlockEntity getCell(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PiglinBarteringCellBlockEntity cell) {
            return cell;
        }
        return null;
    }
}
