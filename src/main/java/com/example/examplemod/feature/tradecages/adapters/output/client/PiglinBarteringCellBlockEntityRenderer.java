package com.example.examplemod.feature.tradecages.adapters.output.client;

import com.example.examplemod.feature.tradecages.adapters.input.PiglinBarteringCellBlock;
import com.example.examplemod.feature.tradecages.adapters.input.PiglinBarteringCellBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class PiglinBarteringCellBlockEntityRenderer implements BlockEntityRenderer<PiglinBarteringCellBlockEntity, PiglinBarteringCellBlockEntityRenderer.State> {
    private static final float ADULT_DISPLAY_SCALE = 0.30F;
    private static final float BABY_DISPLAY_SCALE = ADULT_DISPLAY_SCALE;
    private static final double ENTITY_BACK_OFFSET = 0.12D;
    private static final double ENTITY_BASE_Y = 0.10D;

    private final EntityRenderDispatcher entityRenderer;

    public PiglinBarteringCellBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
    }

    @Override
    public @NonNull State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
            @NonNull PiglinBarteringCellBlockEntity blockEntity,
            @NonNull State state,
            float partialTicks,
            @NonNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.displayPiglin = null;
        state.scale = ADULT_DISPLAY_SCALE;
        state.facing = blockEntity.getBlockState().getValue(PiglinBarteringCellBlock.FACING);

        Piglin piglin = blockEntity.createPiglinForDisplay();
        if (piglin == null) {
            return;
        }

        orientForCellPreview(piglin, state.facing.toYRot());
        state.displayPiglin = entityRenderer.extractEntity(piglin, partialTicks);
        state.displayPiglin.lightCoords = state.lightCoords;
        state.displayPiglin.shadowRadius = 0.0F;
        state.displayPiglin.shadowPieces.clear();
        state.scale = piglin.isBaby() ? BABY_DISPLAY_SCALE : ADULT_DISPLAY_SCALE;
    }

    @Override
    public void submit(State state, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
        if (state.displayPiglin == null) {
            return;
        }

        double stepX = state.facing.getStepX();
        double stepZ = state.facing.getStepZ();
        poseStack.pushPose();
        poseStack.translate(0.5D - stepX * ENTITY_BACK_OFFSET, ENTITY_BASE_Y, 0.5D - stepZ * ENTITY_BACK_OFFSET);
        poseStack.scale(state.scale, state.scale, state.scale);
        entityRenderer.submit(state.displayPiglin, camera, 0.0D, 0.0D, 0.0D, poseStack, submitNodeCollector);
        poseStack.popPose();
    }

    @Override
    public @NonNull AABB getRenderBoundingBox(PiglinBarteringCellBlockEntity blockEntity) {
        var pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D);
    }

    private static void orientForCellPreview(Piglin piglin, float yaw) {
        piglin.setYRot(yaw);
        piglin.setXRot(0.0F);
        piglin.yRotO = yaw;
        piglin.xRotO = 0.0F;
        piglin.yHeadRot = yaw;
        piglin.yHeadRotO = yaw;
        piglin.yBodyRot = yaw;
        piglin.yBodyRotO = yaw;
    }

    public static final class State extends BlockEntityRenderState {
        public @Nullable EntityRenderState displayPiglin;
        public float scale = ADULT_DISPLAY_SCALE;
        public Direction facing = Direction.NORTH;
    }
}
