package com.example.examplemod.feature.tradecages.adapters.output.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

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


    static int sampleCageLightCoords(Level level, BlockPos pos) {
        /*
         * Dynamic uniform cage lighting.
         *
         * We sample the strongest world light around the transparent cage and apply
         * that same packed light to the whole custom-rendered cage model and contents.
         * Result: close light sources brighten the full cage; distant light sources
         * darken it naturally; source direction no longer creates black internal faces.
         */
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

    static void suppressPreviewShadows(EntityRenderState state) {
        state.shadowRadius = 0.0F;
        state.shadowPieces.clear();
    }

}
