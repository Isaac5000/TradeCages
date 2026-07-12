package com.cosmocraft.trading_cells.platform.neoforge.client.screen;

import com.cosmocraft.trading_cells.platform.neoforge.bootstrap.TradingCells;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class MachineScreenLayout {
    public static final int WIDTH = 176;
    public static final int HEIGHT = 222;
    private static final int PLAYER_INVENTORY_Y = 134;
    private static final int PLAYER_INVENTORY_HEIGHT = HEIGHT - PLAYER_INVENTORY_Y;
    private static final int TILE_SIZE = 16;
    private static final Identifier VILLAGER_FRAME = Identifier.fromNamespaceAndPath(
            TradingCells.MOD_ID,
            "textures/gui/container/villager_breeder.png"
    );

    private MachineScreenLayout() {
    }

    public static void drawBackground(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            Identifier surfaceTexture
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                VILLAGER_FRAME,
                x,
                y,
                0.0F,
                0.0F,
                WIDTH,
                HEIGHT,
                WIDTH,
                HEIGHT
        );
        tile(graphics, surfaceTexture, x + 6, y + 6, 164, 128);
        drawPlayerInventory(graphics, x, y);
    }

    public static void drawSingleTextureBackground(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            Identifier surfaceTexture
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                VILLAGER_FRAME,
                x,
                y,
                0.0F,
                0.0F,
                WIDTH,
                HEIGHT,
                WIDTH,
                HEIGHT
        );
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                surfaceTexture,
                x + 6,
                y + 6,
                0.0F,
                0.0F,
                164,
                128,
                TILE_SIZE,
                TILE_SIZE,
                TILE_SIZE,
                TILE_SIZE
        );
        drawPlayerInventory(graphics, x, y);
    }

    public static void drawPlayerInventory(GuiGraphicsExtractor graphics, int x, int y) {
        blitRegion(
                graphics,
                x,
                y + PLAYER_INVENTORY_Y,
                0,
                PLAYER_INVENTORY_Y,
                WIDTH,
                PLAYER_INVENTORY_HEIGHT
        );
    }

    public static void drawSlot(GuiGraphicsExtractor graphics, int screenX, int screenY, int slotX, int slotY) {
        blitRegion(graphics, screenX + slotX - 3, screenY + slotY - 3, 112, 112, 22, 22);
    }

    public static void drawProgressFrame(GuiGraphicsExtractor graphics, int x, int y) {
        blitRegion(graphics, x, y, 53, 99, 70, 13);
    }

    private static void tile(
            GuiGraphicsExtractor graphics,
            Identifier texture,
            int x,
            int y,
            int width,
            int height
    ) {
        for (int offsetY = 0; offsetY < height; offsetY += TILE_SIZE) {
            int tileHeight = Math.min(TILE_SIZE, height - offsetY);
            for (int offsetX = 0; offsetX < width; offsetX += TILE_SIZE) {
                int tileWidth = Math.min(TILE_SIZE, width - offsetX);
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        texture,
                        x + offsetX,
                        y + offsetY,
                        0.0F,
                        0.0F,
                        tileWidth,
                        tileHeight,
                        TILE_SIZE,
                        TILE_SIZE
                );
            }
        }
    }

    private static void blitRegion(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                VILLAGER_FRAME,
                x,
                y,
                u,
                v,
                width,
                height,
                WIDTH,
                HEIGHT
        );
    }
}
