package com.cosmocraft.trading_cells.feature.incubators.adapters.input;

import com.cosmocraft.trading_cells.feature.incubators.adapters.output.IncubatorRegistrationAdapter;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class VillagerIncubatorBlock extends IncubatorBlock {
    public static final MapCodec<VillagerIncubatorBlock> CODEC = simpleCodec(VillagerIncubatorBlock::new);

    public VillagerIncubatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new VillagerIncubatorBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends IncubatorBlockEntity> getBlockEntityType() {
        return IncubatorRegistrationAdapter.VILLAGER_INCUBATOR_BLOCK_ENTITY.get();
    }
}
