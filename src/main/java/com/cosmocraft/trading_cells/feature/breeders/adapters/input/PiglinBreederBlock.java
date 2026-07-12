package com.cosmocraft.trading_cells.feature.breeders.adapters.input;

import com.cosmocraft.trading_cells.feature.breeders.adapters.output.BreederRegistrationAdapter;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class PiglinBreederBlock extends BreederBlock {
    public static final MapCodec<PiglinBreederBlock> CODEC = simpleCodec(PiglinBreederBlock::new);

    public PiglinBreederBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new PiglinBreederBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends BreederBlockEntity> getBlockEntityType() {
        return BreederRegistrationAdapter.PIGLIN_BREEDER_BLOCK_ENTITY.get();
    }
}
