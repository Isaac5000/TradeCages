package com.example.examplemod.feature.breeders.adapters.output.client;

import com.example.examplemod.feature.breeders.adapters.input.BreederBlockEntity;
import com.example.examplemod.feature.breeders.domain.model.BreederKind;
import com.example.examplemod.feature.tradecages.adapters.input.PiglinCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.output.TradingCellsRegistrationAdapter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class BlockItemContentRenderSupport {
    private static final AtomicInteger NEXT_PREVIEW_ENTITY_ID = new AtomicInteger(-3_000_000);

    private static final String ENTITY_KIND_TAG = "StoredEntityKind";
    private static final String ENTITY_DATA_TAG = "StoredEntity";
    private static final String VILLAGER_KIND = "villager";
    private static final String PIGLIN_KIND = "piglin";
    private static final String LEGACY_VILLAGER_DATA_TAG = "StoredVillager";
    private static final String LEGACY_PIGLIN_DATA_TAG = "StoredPiglin";
    private static final String POI_STACK_TAG = "StoredPoi";
    private static final String OUTPUT_BUFFER_TAG = "OutputBuffer";
    private static final String BARTER_TICKS_TAG = "BarterTicksRemaining";
    private static final String PENDING_BABIES_TAG = "PendingBabies";
    private static final String BABY_TEMPLATE_TAG = "BabyTemplate";

    private BlockItemContentRenderSupport() {
    }

    public enum SpecialProfile {
        DEFAULT,
        GUI
    }

    public static final class Renderer implements SpecialModelRenderer<ItemStack> {
        private final SpecialProfile profile;

        private Renderer(SpecialProfile profile) {
            this.profile = profile;
        }
        @Override
        public ItemStack extractArgument(@NonNull ItemStack stack) {
            return stack;
        }

        @Override
        public void submit(ItemStack stack, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int packedLight, int packedOverlay, boolean hasFoil, int seed) {
            if (stack == null) {
                return;
            }

            Level level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }

            TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (data == null) {
                return;
            }

            CompoundTag tag = data.copyTagWithoutId();
            String itemId = stack.getItem().builtInRegistryHolder().key().identifier().getPath();

            poseStack.pushPose();
            applyProfileTransform(poseStack, profile);
            if ("villager_breeder".equals(itemId)) {
                renderBreederContents(level, tag, BreederKind.VILLAGER, poseStack, submitNodeCollector, packedLight);
            } else if ("piglin_breeder".equals(itemId)) {
                renderBreederContents(level, tag, BreederKind.PIGLIN, poseStack, submitNodeCollector, packedLight);
            } else if ("villager_trading_cell".equals(itemId)) {
                renderTradingCellContents(level, tag, BreederKind.VILLAGER, poseStack, submitNodeCollector, packedLight, seed);
            } else if ("piglin_bartering_cell".equals(itemId)) {
                renderTradingCellContents(level, tag, BreederKind.PIGLIN, poseStack, submitNodeCollector, packedLight, seed);
            }
            poseStack.popPose();
        }

        @Override
        public void getExtents(Consumer<Vector3fc> extents) {
            extents.accept(new Vector3f(-0.75F, -0.25F, -0.75F));
            extents.accept(new Vector3f(0.75F, 1.25F, 0.75F));
        }
    }

    private static void renderBreederContents(Level level, CompoundTag tag, BreederKind kind, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
        ItemStack parentA = readStoredStack(level, tag, "Slot" + BreederBlockEntity.PARENT_A_SLOT);
        ItemStack parentB = readStoredStack(level, tag, "Slot" + BreederBlockEntity.PARENT_B_SLOT);

        renderBreederBed(kind, poseStack, submitNodeCollector, packedLight);
        renderCapturedEntity(level, parentA, kind, 0.22D, 0.06D, 0.00D, 90.0F, 0.23F, poseStack, submitNodeCollector, packedLight);
        renderCapturedEntity(level, parentB, kind, -0.22D, 0.06D, 0.00D, 270.0F, 0.23F, poseStack, submitNodeCollector, packedLight);
    }

    private static ItemStack createPendingBabyStack(CompoundTag tag, BreederKind kind) {
        int pendingBabies = tag.getInt(PENDING_BABIES_TAG).orElse(0);
        if (pendingBabies <= 0) {
            return ItemStack.EMPTY;
        }

        CompoundTag babyTemplate = tag.getCompound(BABY_TEMPLATE_TAG).map(CompoundTag::copy).orElse(null);
        if (babyTemplate == null || babyTemplate.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(kind == BreederKind.VILLAGER
                ? TradingCellsRegistrationAdapter.VILLAGER_CAPTURER_ITEM.get()
                : TradingCellsRegistrationAdapter.PIGLIN_CAPTURER_ITEM.get());
        if (kind == BreederKind.VILLAGER) {
            VillagerCapturerItem.setCapturedVillagerData(stack, babyTemplate);
        } else {
            PiglinCapturerItem.setCapturedPiglinData(stack, babyTemplate);
        }
        stack.setCount(Math.min(pendingBabies, stack.getMaxStackSize()));
        return stack;
    }

    private static void renderTradingCellContents(Level level, CompoundTag tag, BreederKind kind, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, int seed) {
        CompoundTag entityData = readStoredEntityData(tag, kind);
        if (entityData != null) {
            Entity entity = kind == BreederKind.VILLAGER
                    ? VillagerCapturerItem.createCapturedVillager(level, entityData, BlockPos.ZERO)
                    : PiglinCapturerItem.createCapturedPiglin(level, entityData, BlockPos.ZERO);
            if (entity instanceof Piglin piglin && tag.getInt(BARTER_TICKS_TAG).orElse(0) > 0) {
                piglin.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.GOLD_INGOT));
            }
            renderEntity(entity, 0.0D, 0.05D, 0.05D, 180.0F, 0.26666668F, poseStack, submitNodeCollector, packedLight);
        }

        if (kind == BreederKind.VILLAGER) {
            ItemStack poiStack = readStoredStack(level, tag, POI_STACK_TAG);
            renderMiniBlock(poiStack, 0.0D, 0.15D, -0.16D, 0.24F, poseStack, submitNodeCollector, packedLight);
        } else {
            ItemStack outputStack = readStoredStack(level, tag, OUTPUT_BUFFER_TAG);
            renderItemStack(level, outputStack, 0.0D, 0.35D, -0.18D, 0.28F, poseStack, submitNodeCollector, packedLight, seed);
        }
    }


    private static void applyProfileTransform(PoseStack poseStack, SpecialProfile profile) {
        if (profile == SpecialProfile.GUI) {
            poseStack.translate(0.5D, 0.02D, 0.5D);
            poseStack.scale(0.92F, 0.92F, 0.92F);
            poseStack.translate(-0.5D, 0.0D, -0.5D);
        }
    }

    private static void renderBreederBed(BreederKind kind, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
        renderBedPart(BreederBlockEntityRenderer.createBedState(kind, Direction.SOUTH, BedPart.FOOT), Direction.SOUTH, false, poseStack, submitNodeCollector, packedLight);
        renderBedPart(BreederBlockEntityRenderer.createBedState(kind, Direction.SOUTH, BedPart.HEAD), Direction.SOUTH, true, poseStack, submitNodeCollector, packedLight);
    }

    private static void renderBedPart(BlockState bedBlockState, Direction facing, boolean head, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
        BlockModelRenderState bedState = new BlockModelRenderState();
        BlockModelResolver resolver = Minecraft.getInstance().getBlockModelResolver();
        resolver.update(bedState, bedBlockState, BlockDisplayContext.create());
        bedState.tintLayers().clear();
        if (bedState.isEmpty()) {
            return;
        }

        final double bedScale = 0.26D;
        final double bedY = 0.12D;
        double partOffset = (head ? 0.5D : -0.5D) * bedScale;
        double x = 0.5D - bedScale * 0.5D + facing.getStepX() * partOffset;
        double z = 0.5D - bedScale * 0.5D + facing.getStepZ() * partOffset;

        poseStack.pushPose();
        poseStack.translate(x, bedY, z);
        poseStack.scale((float) bedScale, (float) bedScale, (float) bedScale);
        bedState.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
        poseStack.popPose();
    }
    private static @Nullable CompoundTag readStoredEntityData(CompoundTag tag, BreederKind kind) {
        CompoundTag entityData = null;
        if (tag.getCompound(ENTITY_DATA_TAG).isPresent()) {
            String entityKind = tag.getString(ENTITY_KIND_TAG).orElse("");
            if ((kind == BreederKind.VILLAGER && VILLAGER_KIND.equals(entityKind)) || (kind == BreederKind.PIGLIN && PIGLIN_KIND.equals(entityKind))) {
                entityData = tag.getCompound(ENTITY_DATA_TAG).map(CompoundTag::copy).orElse(null);
            }
        }

        if (entityData == null) {
            entityData = tag.getCompound(kind == BreederKind.VILLAGER ? LEGACY_VILLAGER_DATA_TAG : LEGACY_PIGLIN_DATA_TAG).map(CompoundTag::copy).orElse(null);
        }
        return entityData;
    }

    private static ItemStack readStoredStack(Level level, CompoundTag tag, String key) {
        CompoundTag stackTag = tag.getCompound(key).orElse(null);
        if (stackTag == null || stackTag.isEmpty()) {
            return ItemStack.EMPTY;
        }

        DynamicOps<Tag> ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.OPTIONAL_CODEC.parse(ops, stackTag).result().orElse(ItemStack.EMPTY);
    }

    private static void renderCapturedEntity(Level level, ItemStack stack, BreederKind kind, double x, double y, double z, float yaw, float scale, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
        if (stack.isEmpty()) {
            return;
        }
        Entity entity = kind == BreederKind.VILLAGER
                ? VillagerCapturerItem.createCapturedVillager(level, stack, BlockPos.ZERO)
                : PiglinCapturerItem.createCapturedPiglin(level, stack, BlockPos.ZERO);
        renderEntity(entity, x, y, z, yaw, scale, poseStack, submitNodeCollector, packedLight);
    }

    private static void renderEntity(@Nullable Entity entity, double x, double y, double z, float yaw, float scale, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
        if (entity == null) {
            return;
        }

        orient(entity, yaw);
        preparePreviewEntity(entity);

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderState state = dispatcher.extractEntity(entity, 0.0F);
        state.lightCoords = packedLight;
        state.shadowRadius = 0.0F;
        state.shadowPieces.clear();
        state.displayFireAnimation = false;
        state.nameTag = null;

        poseStack.pushPose();
        poseStack.translate(0.5D + x, 0.10D + y, 0.5D + z);
        poseStack.scale(scale, scale, scale);
        dispatcher.submit(state, new CameraRenderState(), 0.0D, 0.0D, 0.0D, poseStack, submitNodeCollector);
        poseStack.popPose();
    }

    private static void renderMiniBlock(ItemStack stack, double x, double y, double z, float scale, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        BlockState blockState = orientBlockState(blockItem.getBlock().defaultBlockState(), Direction.SOUTH);
        BlockModelRenderState renderState = new BlockModelRenderState();
        BlockModelResolver resolver = Minecraft.getInstance().getBlockModelResolver();
        resolver.update(renderState, blockState, BlockDisplayContext.create());
        renderState.tintLayers().clear();
        if (renderState.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D + x - scale * 0.5D, y, 0.5D + z - scale * 0.5D);
        poseStack.scale(scale, scale, scale);
        renderState.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
        poseStack.popPose();
    }

    private static void renderItemStack(Level level, ItemStack stack, double x, double y, double z, float scale, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, int seed) {
        if (stack.isEmpty()) {
            return;
        }

        ItemStackRenderState renderState = new ItemStackRenderState();
        ItemModelResolver resolver = Minecraft.getInstance().getItemModelResolver();
        resolver.updateForTopItem(renderState, stack, ItemDisplayContext.FIXED, level, null, seed);
        if (renderState.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D + x, y, 0.5D + z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(scale, scale, scale);
        renderState.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
        poseStack.popPose();
    }

    private static BlockState orientBlockState(BlockState state, Direction facing) {
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.setValue(BlockStateProperties.FACING, facing);
        }
        return state;
    }

    private static void orient(Entity entity, float yaw) {
        entity.setYRot(yaw);
        entity.setXRot(0.0F);
        entity.yRotO = yaw;
        entity.xRotO = 0.0F;
        if (entity instanceof LivingEntity living) {
            living.yHeadRot = yaw;
            living.yHeadRotO = yaw;
            living.yBodyRot = yaw;
            living.yBodyRotO = yaw;
        }
    }

    private static void preparePreviewEntity(Entity entity) {
        entity.setId(NEXT_PREVIEW_ENTITY_ID.getAndDecrement());
        entity.setNoGravity(true);
        entity.clearFire();
        entity.setSilent(true);
        entity.setInvisible(false);
    }

    public static final class DefaultUnbaked {
        private static final ConfigurableUnbaked INSTANCE = new ConfigurableUnbaked(() -> new Renderer(SpecialProfile.DEFAULT));
        public static final MapCodec<? extends SpecialModelRenderer.Unbaked<SpecialModelRenderer>> MAP_CODEC = INSTANCE.type();

        private DefaultUnbaked() {
        }
    }

    public static final class GuiUnbaked {
        private static final ConfigurableUnbaked INSTANCE = new ConfigurableUnbaked(() -> new Renderer(SpecialProfile.GUI));
        public static final MapCodec<? extends SpecialModelRenderer.Unbaked<SpecialModelRenderer>> MAP_CODEC = INSTANCE.type();

        private GuiUnbaked() {
        }
    }

    private static final class ConfigurableUnbaked implements SpecialModelRenderer.Unbaked<SpecialModelRenderer> {
        private final java.util.function.Supplier<SpecialModelRenderer> factory;
        private final MapCodec<? extends SpecialModelRenderer.Unbaked<SpecialModelRenderer>> codec;

        private ConfigurableUnbaked(java.util.function.Supplier<SpecialModelRenderer> factory) {
            this.factory = factory;
            this.codec = MapCodec.unit(this);
        }

        @Override
        public @NonNull MapCodec<? extends SpecialModelRenderer.Unbaked<SpecialModelRenderer>> type() {
            return this.codec;
        }

        @Override
        public SpecialModelRenderer bake(SpecialModelRenderer.@NonNull BakingContext bakingContext) {
            return this.factory.get();
        }
    }
}
