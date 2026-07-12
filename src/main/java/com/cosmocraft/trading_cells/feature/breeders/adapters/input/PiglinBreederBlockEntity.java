package com.cosmocraft.trading_cells.feature.breeders.adapters.input;

import com.cosmocraft.trading_cells.feature.breeders.domain.model.BreederKind;
import com.cosmocraft.trading_cells.feature.breeders.adapters.output.BreederRegistrationAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class PiglinBreederBlockEntity extends BreederBlockEntity {
    public PiglinBreederBlockEntity(BlockPos pos, BlockState blockState) {
        super(BreederRegistrationAdapter.PIGLIN_BREEDER_BLOCK_ENTITY.get(), pos, blockState, BreederKind.PIGLIN);
    }
}
