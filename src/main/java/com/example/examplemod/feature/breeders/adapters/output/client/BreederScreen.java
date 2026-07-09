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

    public BreederScreen(BreederMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 188);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelY = 94; // matches default GUIs (imageHeight - 94 when imageHeight == 188)
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        // use leftPos/topPos provided by AbstractContainerScreen so the GUI is properly aligned
        int x = this.leftPos;
        int y = this.topPos;
        Identifier texture = this.menu.kind() == BreederKind.VILLAGER ? VILLAGER_BACKGROUND : PIGLIN_BACKGROUND;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        int progress = this.menu.maxBreedTicks() <= 0 ? 0 : Math.min(24, this.menu.breedTicks() * 24 / this.menu.maxBreedTicks());
        if (progress > 0) {
            graphics.fill(x + 72, y + 82, x + 72 + progress, y + 87, this.menu.kind() == BreederKind.VILLAGER ? 0xFF52D45A : 0xFFFF5A6A);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        // Use leftPos/topPos offsets so labels align with the background texture
        int x = this.leftPos;
        int y = this.topPos;
        // title label
        graphics.text(this.font, this.title, x + this.titleLabelX, y + this.titleLabelY, 0x3B2A18, false);
        // pending babies label
        graphics.text(this.font, Component.translatable("label.trading_cells.pending_babies", this.menu.pendingBabies()), x + 10, y + 90, this.menu.kind() == BreederKind.VILLAGER ? 0x3B2A18 : 0xE6B0A8, false);
        // inventory label (left side)
        graphics.text(this.font, Component.translatable("container.inventory"), x + 8, y + this.inventoryLabelY, 0x3B2A18, false);
    }
}
