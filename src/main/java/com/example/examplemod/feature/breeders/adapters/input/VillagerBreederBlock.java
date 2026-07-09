package com.example.examplemod.feature.breeders.adapters.input;

import com.example.examplemod.feature.breeders.adapters.output.BreederRegistrationAdapter;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class VillagerBreederBlock extends BreederBlock {
    public static final MapCodec<VillagerBreederBlock> CODEC = simpleCodec(VillagerBreederBlock::new);

    public VillagerBreederBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new VillagerBreederBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends BreederBlockEntity> getBlockEntityType() {
        return BreederRegistrationAdapter.VILLAGER_BREEDER_BLOCK_ENTITY.get();
    }
}
