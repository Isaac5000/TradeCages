package com.example.examplemod.feature.tradecages.adapters.output.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ajustes comunes para renderizar entidades como preview dentro de items especiales
 * y block entities en NeoForge 26.2+.
 *
 * En esta versión el render de entidades se hace a través de EntityRenderState. Si
 * reutilizamos el estado tal cual, también se arrastran efectos de mundo como overlay
 * rojo, fuego, nametag, leash y sombras. Para previews dentro de esferas/cajas eso
 * produce tintes y artefactos visuales, así que limpiamos el estado antes de enviarlo.
 */
final class PreviewEntityRenderUtil {
    /**
     * NeoForge/Minecraft 26.2 throws when EntityRenderDispatcher extracts an entity
     * whose id is still 0. Preview entities created from item/block NBT are not added
     * to the world, so they never receive a real id. Use negative ids for client-only
     * previews; they cannot collide with normal server-assigned entity ids.
     */
    private static final AtomicInteger NEXT_PREVIEW_ENTITY_ID = new AtomicInteger(-1_000_000);

    private PreviewEntityRenderUtil() {
    }


    static void preparePreviewEntity(Entity entity) {
        entity.setId(NEXT_PREVIEW_ENTITY_ID.getAndDecrement());
        entity.setNoGravity(true);
        entity.clearFire();
        entity.setSilent(true);
        entity.setInvisible(false);
    }

    static void sanitizePreviewState(EntityRenderState state, int lightCoords) {
        state.lightCoords = lightCoords;
        state.outlineColor = EntityRenderState.NO_OUTLINE;
        state.displayFireAnimation = false;
        state.isInvisible = false;
        state.isDiscrete = false;
        state.nameTag = null;
        state.scoreText = null;
        state.nameTagAttachment = null;
        state.passengerOffset = null;
        state.leashStates = null;
        state.shadowRadius = 0.0F;
        state.shadowPieces.clear();

        if (state instanceof LivingEntityRenderState livingState) {
            livingState.deathTime = 0.0F;
            livingState.walkAnimationPos = 0.0F;
            livingState.walkAnimationSpeed = 0.0F;
            livingState.ticksSinceKineticHitFeedback = 0.0F;
            livingState.isUpsideDown = false;
            livingState.isFullyFrozen = false;
            livingState.isAutoSpinAttack = false;
            livingState.hasRedOverlay = false;
            livingState.isInvisibleToPlayer = false;
            livingState.bedOrientation = null;
            livingState.pose = Pose.STANDING;
        }
    }

    static CameraRenderState cameraForPreview(boolean guiPreview) {
        Minecraft minecraft = Minecraft.getInstance();
        CameraRenderState currentCamera = minecraft.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
        if (currentCamera != null && currentCamera.initialized && !guiPreview) {
            return currentCamera;
        }

        CameraRenderState fallbackCamera = new CameraRenderState();
        fallbackCamera.initialized = true;
        return fallbackCamera;
    }
}
