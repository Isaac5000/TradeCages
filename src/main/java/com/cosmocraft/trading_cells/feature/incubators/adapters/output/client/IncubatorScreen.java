package com.cosmocraft.trading_cells.feature.incubators.adapters.output.client;

import com.cosmocraft.trading_cells.feature.incubators.adapters.input.IncubatorMenu;
import com.cosmocraft.trading_cells.feature.incubators.domain.model.IncubatorKind;
import com.cosmocraft.trading_cells.platform.neoforge.bootstrap.TradingCells;
import com.cosmocraft.trading_cells.platform.neoforge.client.screen.MachineScreenLayout;
import com.cosmocraft.trading_cells.platform.neoforge.client.screen.MachineScreenUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class IncubatorScreen extends AbstractContainerScreen<IncubatorMenu> {
    private static final Identifier VILLAGER_FRAME = Identifier.fromNamespaceAndPath(
            TradingCells.MOD_ID,
            "textures/gui/container/villager_breeder.png"
    );
    private static final Identifier PIGLIN_FRAME = Identifier.fromNamespaceAndPath(
            TradingCells.MOD_ID,
            "textures/gui/container/piglin_breeder.png"
    );
    private static final Identifier YELLOW_WOOL = Identifier.fromNamespaceAndPath(
            "minecraft",
            "textures/block/yellow_wool.png"
    );
    private static final Identifier RED_WOOL = Identifier.fromNamespaceAndPath(
            "minecraft",
            "textures/block/red_wool.png"
    );
    private static final int WIDTH = 176;
    private static final int HEIGHT = 222;
    private static final int TILE_SIZE = 16;
    private static final int PROGRESS_X = 54;
    private static final int PROGRESS_Y = 81;
    private static final int PROGRESS_WIDTH = 68;
    private static final int PROGRESS_HEIGHT = 8;

    public IncubatorScreen(IncubatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 126;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        Identifier frame = menu.kind() == IncubatorKind.VILLAGER ? VILLAGER_FRAME : PIGLIN_FRAME;
        Identifier wool = menu.kind() == IncubatorKind.VILLAGER ? YELLOW_WOOL : RED_WOOL;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                frame,
                x,
                y,
                0.0F,
                0.0F,
                WIDTH,
                HEIGHT,
                WIDTH,
                HEIGHT
        );
        tile(graphics, wool, x + 6, y + 6, 164, 128);
        MachineScreenLayout.drawPlayerInventory(graphics, x, y);
        blitRegion(graphics, frame, x + 42, y + 45, 112, 112, 22, 22);
        blitRegion(graphics, frame, x + 112, y + 45, 112, 112, 22, 22);
        blitRegion(graphics, frame, x + 53, y + 78, 53, 99, 70, 13);

        int progress = menu.maxIncubationTicks() <= 0
                ? 0
                : Math.min(PROGRESS_WIDTH, menu.incubationTicks() * PROGRESS_WIDTH / menu.maxIncubationTicks());
        if (progress > 0) {
            int color = menu.kind() == IncubatorKind.VILLAGER ? 0xFFF0C934 : 0xFFD84B45;
            graphics.fill(x + PROGRESS_X, y + PROGRESS_Y, x + PROGRESS_X + progress, y + PROGRESS_Y + PROGRESS_HEIGHT, color);
        }
        if (menu.incubationTicks() > 0) {
            MachineScreenUtil.drawCenteredCountdown(
                    graphics,
                    font,
                    x + PROGRESS_X + PROGRESS_WIDTH / 2,
                    y + PROGRESS_Y - 1,
                    menu.incubationTicks(),
                    menu.maxIncubationTicks()
            );
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0x303030, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x303030, false);
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
            Identifier texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
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
