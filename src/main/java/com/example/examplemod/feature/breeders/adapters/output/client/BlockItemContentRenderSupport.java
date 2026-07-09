package com.example.examplemod.feature.breeders.adapters.output.client;

import com.example.examplemod.feature.breeders.adapters.input.BreederBlockEntity;
import com.example.examplemod.feature.breeders.domain.model.BreederKind;
import com.example.examplemod.feature.tradecages.adapters.input.PiglinCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicInteger;

public final class BlockItemContentRenderSupport {
    private static final AtomicInteger NEXT_PREVIEW_ENTITY_ID = new AtomicInteger(-3_000_000);

    private static final String ENTITY_KIND_TAG = "StoredEntityKind";
    private static final String ENTITY_DATA_TAG = "StoredEntity";
    private static final String VILLAGER_KIND = "villager";
    private static final String PIGLIN_KIND = "piglin";
    private static final String LEGACY_VILLAGER_DATA_TAG = "StoredVillager";
    private static final String LEGACY_PIGLIN_DATA_TAG = "StoredPiglin";

    private BlockItemContentRenderSupport() {
    }

    public static final class Renderer implements SpecialModelRenderer<ItemStack> {
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

            if ("villager_breeder".equals(itemId)) {
                renderBreederContents(level, tag, BreederKind.VILLAGER, poseStack, submitNodeCollector, packedLight);
            } else if ("piglin_breeder".equals(itemId)) {
                renderBreederContents(level, tag, BreederKind.PIGLIN, poseStack, submitNodeCollector, packedLight);
            } else if ("villager_trading_cell".equals(itemId)) {
                renderTradingCellContents(level, tag, BreederKind.VILLAGER, poseStack, submitNodeCollector, packedLight);
            } else if ("piglin_bartering_cell".equals(itemId)) {
                renderTradingCellContents(level, tag, BreederKind.PIGLIN, poseStack, submitNodeCollector, packedLight);
            }
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
        ItemStack baby = readStoredStack(level, tag, "Slot" + BreederBlockEntity.FILLED_CAPTURER_SLOT);

        renderCapturedEntity(level, parentA, kind, 0.22D, 0.06D, 0.00D, kind == BreederKind.VILLAGER ? 90.0F : 90.0F, 0.17F, poseStack, submitNodeCollector, packedLight);
        renderCapturedEntity(level, parentB, kind, -0.22D, 0.06D, 0.00D, kind == BreederKind.VILLAGER ? 270.0F : 270.0F, 0.17F, poseStack, submitNodeCollector, packedLight);
        if (!baby.isEmpty()) {
            renderCapturedEntity(level, baby, kind, 0.0D, 0.06D, 0.25D, 180.0F, 0.15F, poseStack, submitNodeCollector, packedLight);
        }
    }

    private static void renderTradingCellContents(Level level, CompoundTag tag, BreederKind kind, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
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
        if (entityData == null) {
            return;
        }

        Entity entity = kind == BreederKind.VILLAGER
                ? VillagerCapturerItem.createCapturedVillager(level, entityData, BlockPos.ZERO)
                : PiglinCapturerItem.createCapturedPiglin(level, entityData, BlockPos.ZERO);
        renderEntity(level, entity, 0.0D, 0.05D, 0.05D, 180.0F, 0.20F, poseStack, submitNodeCollector, packedLight);
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
        renderEntity(level, entity, x, y, z, yaw, scale, poseStack, submitNodeCollector, packedLight);
    }

    private static void renderEntity(Level level, @Nullable Entity entity, double x, double y, double z, float yaw, float scale, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
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
        dispatcher.submit(state, new net.minecraft.client.renderer.state.level.CameraRenderState(), 0.0D, 0.0D, 0.0D, poseStack, submitNodeCollector);
        poseStack.popPose();
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

    public static final class Unbaked implements SpecialModelRenderer.Unbaked<SpecialModelRenderer> {
        private static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<? extends SpecialModelRenderer.Unbaked<SpecialModelRenderer>> MAP_CODEC = MapCodec.unit(INSTANCE);

        private Unbaked() {
        }

        @Override
        public @NonNull MapCodec<? extends SpecialModelRenderer.Unbaked<SpecialModelRenderer>> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer bake(SpecialModelRenderer.@NonNull BakingContext bakingContext) {
            return new Renderer();
        }
    }
}
