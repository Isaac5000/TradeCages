package com.example.examplemod.feature.breeders.adapters.output.client;

import com.example.examplemod.feature.breeders.adapters.input.BreederBlock;
import com.example.examplemod.feature.breeders.adapters.input.BreederBlockEntity;
import com.example.examplemod.feature.breeders.domain.model.BreederKind;
import com.example.examplemod.feature.tradecages.adapters.input.PiglinCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public final class BreederBlockEntityRenderer implements BlockEntityRenderer<BreederBlockEntity, BreederBlockEntityRenderer.State> {
    // Adjusted scales so villagers and beds display at natural size inside breeder
    // Scales halved from previous values to avoid oversized preview
    private static final float ADULT_SCALE = 0.32F;
    private static final float BABY_SCALE = 0.25F;
    private static final double PARENT_SIDE_OFFSET = 0.23D;
    private static final double PARENT_BACK_OFFSET = 0.05D;
    private static final double ENTITY_Y = 0.11D;
    private static final double BED_Y = 0.12D;
    private static final double BED_SCALE = 0.26D;
    private static final AtomicInteger NEXT_PREVIEW_ENTITY_ID = new AtomicInteger(-2_000_000);

    private final EntityRenderDispatcher entityRenderer;
    private final BlockModelResolver blockModelResolver;

    public BreederBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public @NonNull State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
            @NonNull BreederBlockEntity blockEntity,
            @NonNull State state,
            float partialTicks,
            @NonNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = blockEntity.getBlockState().getValue(BreederBlock.FACING);
        state.kind = blockEntity.kind();
        state.parentA = null;
        state.parentB = null;
        state.baby = null;
        state.bedFoot.clear();
        state.bedHead.clear();

        Level level = blockEntity.getLevel();
        if (level != null) {
            state.lightCoords = sampleCageLightCoords(level, blockEntity.getBlockPos());

            BlockState bedFoot = createBedState(state.kind, state.facing, BedPart.FOOT);
            BlockState bedHead = createBedState(state.kind, state.facing, BedPart.HEAD);
            blockModelResolver.update(state.bedFoot, bedFoot, BlockDisplayContext.create());
            blockModelResolver.update(state.bedHead, bedHead, BlockDisplayContext.create());
            state.bedFoot.tintLayers().clear();
            state.bedHead.tintLayers().clear();

            state.parentA = extractParent(level, blockEntity.getItem(BreederBlockEntity.PARENT_A_SLOT), state.kind, state.facing.getClockWise(), partialTicks, state.lightCoords);
            state.parentB = extractParent(level, blockEntity.getItem(BreederBlockEntity.PARENT_B_SLOT), state.kind, state.facing.getCounterClockWise(), partialTicks, state.lightCoords);

            ItemStack babyStack = blockEntity.createBabyPreviewStack();
            state.baby = extractParent(level, babyStack, state.kind, state.facing, partialTicks, state.lightCoords);
        }
    }

    static BlockState createBedState(BreederKind kind, Direction facing, BedPart part) {
        return (kind == BreederKind.VILLAGER ? Blocks.BED.yellow() : Blocks.BED.red())
                .defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BedBlock.PART, part);
    }

    @Override
    public void submit(State state, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
        submitBedPart(state.bedFoot, state.facing, false, poseStack, submitNodeCollector, state.lightCoords);
        submitBedPart(state.bedHead, state.facing, true, poseStack, submitNodeCollector, state.lightCoords);

        Direction side = state.facing.getClockWise();
        double sideX = side.getStepX();
        double sideZ = side.getStepZ();
        double backX = -state.facing.getStepX() * PARENT_BACK_OFFSET;
        double backZ = -state.facing.getStepZ() * PARENT_BACK_OFFSET;

        submitEntity(state.parentA, state.parentAScale, 0.5D - sideX * PARENT_SIDE_OFFSET + backX, ENTITY_Y, 0.5D - sideZ * PARENT_SIDE_OFFSET + backZ, poseStack, submitNodeCollector, camera);
        submitEntity(state.parentB, state.parentBScale, 0.5D + sideX * PARENT_SIDE_OFFSET + backX, ENTITY_Y, 0.5D + sideZ * PARENT_SIDE_OFFSET + backZ, poseStack, submitNodeCollector, camera);
    }

    private static void submitBedPart(BlockModelRenderState bedState, Direction facing, boolean head, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        if (bedState.isEmpty()) {
            return;
        }

        double partOffset = (head ? 0.5D : -0.5D) * BED_SCALE;
        double x = 0.5D - BED_SCALE * 0.5D + facing.getStepX() * partOffset;
        double z = 0.5D - BED_SCALE * 0.5D + facing.getStepZ() * partOffset;

        poseStack.pushPose();
        poseStack.translate(x, BED_Y, z);
        poseStack.scale((float) BED_SCALE, (float) BED_SCALE, (float) BED_SCALE);
        bedState.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
        poseStack.popPose();
    }

    private void submitEntity(@Nullable EntityRenderState entityState, float scale, double x, double y, double z, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (entityState == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);
        entityRenderer.submit(entityState, camera, 0.0D, 0.0D, 0.0D, poseStack, submitNodeCollector);
        poseStack.popPose();
    }

    private @Nullable EntityRenderState extractParent(Level level, ItemStack stack, BreederKind kind, Direction lookDirection, float partialTicks, int lightCoords) {
        Entity entity = null;
        if (kind == BreederKind.VILLAGER) {
            CompoundTag data = VillagerCapturerItem.getCapturedVillagerData(stack);
            if (data != null) {
                entity = VillagerCapturerItem.createCapturedVillager(level, data, BlockPos.ZERO);
            }
        } else {
            CompoundTag data = PiglinCapturerItem.getCapturedPiglinData(stack);
            if (data != null) {
                entity = PiglinCapturerItem.createCapturedPiglin(level, data, BlockPos.ZERO);
            }
        }

        if (entity == null) {
            return null;
        }

        orientForBreeder(entity, lookDirection.toYRot());
        preparePreviewEntity(entity);
        EntityRenderState renderState = entityRenderer.extractEntity(entity, partialTicks);
        renderState.lightCoords = lightCoords;
        suppressPreviewShadows(renderState);
        return renderState;
    }

    private static void orientForBreeder(Entity entity, float yaw) {
        entity.setYRot(yaw);
        entity.setXRot(0.0F);
        entity.yRotO = yaw;
        entity.xRotO = 0.0F;
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.yHeadRot = yaw;
            livingEntity.yHeadRotO = yaw;
            livingEntity.yBodyRot = yaw;
            livingEntity.yBodyRotO = yaw;
        }
    }

    private static void preparePreviewEntity(Entity entity) {
        entity.setId(NEXT_PREVIEW_ENTITY_ID.getAndDecrement());
        entity.setNoGravity(true);
        entity.clearFire();
        entity.setSilent(true);
        entity.setInvisible(false);
    }

    private static void suppressPreviewShadows(EntityRenderState state) {
        state.shadowRadius = 0.0F;
        state.shadowPieces.clear();
        state.displayFireAnimation = false;
        state.nameTag = null;
    }

    private static int sampleCageLightCoords(Level level, BlockPos pos) {
        int blockLight = 0;
        int skyLight = 0;
        BlockPos.MutableBlockPos samplePos = new BlockPos.MutableBlockPos();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    samplePos.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    blockLight = Math.max(blockLight, level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, samplePos));
                    skyLight = Math.max(skyLight, level.getBrightness(net.minecraft.world.level.LightLayer.SKY, samplePos));
                }
            }
        }

        blockLight = Math.min(15, blockLight);
        skyLight = Math.min(15, skyLight);
        return (skyLight << 20) | (blockLight << 4);
    }

    @Override
    public @NonNull AABB getRenderBoundingBox(BreederBlockEntity blockEntity) {
        var pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D);
    }

    public static final class State extends BlockEntityRenderState {
        public final BlockModelRenderState bedFoot = new BlockModelRenderState();
        public final BlockModelRenderState bedHead = new BlockModelRenderState();
        public @Nullable EntityRenderState parentA;
        public @Nullable EntityRenderState parentB;
        public @Nullable EntityRenderState baby;
        public float parentAScale = ADULT_SCALE;
        public float parentBScale = ADULT_SCALE;
        public float babyScale = BABY_SCALE;
        public Direction facing = Direction.NORTH;
        public BreederKind kind = BreederKind.VILLAGER;
    }
}
