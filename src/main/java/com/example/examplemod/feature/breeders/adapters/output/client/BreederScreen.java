package com.example.examplemod.feature.breeders.adapters.output.client;

import com.example.examplemod.feature.breeders.adapters.input.BreederMenu;
import com.example.examplemod.feature.breeders.domain.model.BreederKind;
import com.example.examplemod.platform.neoforge.bootstrap.TradingCells;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class BreederScreen extends AbstractContainerScreen<BreederMenu> {
    private static final Identifier VILLAGER_BACKGROUND = Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, "textures/gui/container/villager_breeder.png");
    private static final Identifier PIGLIN_BACKGROUND = Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, "textures/gui/container/piglin_breeder.png");
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 222;
    private static final int PROGRESS_X = 54;
    private static final int PROGRESS_Y = 102;
    private static final int PROGRESS_WIDTH = 68;
    private static final int PROGRESS_HEIGHT = 8;

    public BreederScreen(BreederMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 126;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;
        Identifier texture = this.menu.kind() == BreederKind.VILLAGER ? VILLAGER_BACKGROUND : PIGLIN_BACKGROUND;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                0.0F,
                0.0F,
                this.imageWidth,
                this.imageHeight,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        int progress = this.menu.maxBreedTicks() <= 0
                ? 0
                : Math.min(PROGRESS_WIDTH, this.menu.breedTicks() * PROGRESS_WIDTH / this.menu.maxBreedTicks());
        if (progress > 0) {
            graphics.fill(
                    x + PROGRESS_X,
                    y + PROGRESS_Y,
                    x + PROGRESS_X + progress,
                    y + PROGRESS_Y + PROGRESS_HEIGHT,
                    this.menu.kind() == BreederKind.VILLAGER ? 0xFFE2B23F : 0xFFE04A4A
            );
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // AbstractContainerScreen already translates the pose to leftPos/topPos before labels are drawn.
        // Keep these coordinates local to the GUI or the text will drift/cut off at high GUI scale.
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x3B2A18, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x3B2A18, false);
    }
}
