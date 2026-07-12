package com.cosmocraft.trading_cells.feature.breeders.adapters.output.client;

import com.cosmocraft.trading_cells.feature.breeders.adapters.input.BreederMenu;
import com.cosmocraft.trading_cells.feature.breeders.domain.model.BreederKind;
import com.cosmocraft.trading_cells.platform.neoforge.bootstrap.TradingCells;
import com.cosmocraft.trading_cells.platform.neoforge.client.screen.MachineScreenUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
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
    private static final int MAX_VISIBLE_VARIANTS = 6;
    private static final int VARIANT_LIST_X = 94;
    private static final int VARIANT_LIST_Y = 98;
    private static final int VARIANT_LIST_WIDTH = 74;
    private static final int VARIANT_ROW_HEIGHT = 16;
    private final List<VariantButton> variantButtons = new ArrayList<>();
    private Button variantSelector;
    private boolean variantListOpen;
    private int variantScroll;

    public BreederScreen(BreederMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 126;
    }

    @Override
    protected void init() {
        super.init();
        if (menu.kind() != BreederKind.VILLAGER) {
            return;
        }
        variantSelector = addRenderableWidget(Button.builder(variantSelectorLabel(), button -> toggleVariantList())
                .bounds(leftPos + 102, topPos + 78, 66, 18)
                .build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (variantSelector != null) {
            variantSelector.setMessage(variantSelectorLabel());
        }
        if (variantListOpen
                && variantButtons.size() != Math.min(MAX_VISIBLE_VARIANTS, menu.villagerVariantCount())) {
            closeVariantList();
        }
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
        if (menu.breedTicks() > 0) {
            MachineScreenUtil.drawCenteredCountdown(
                    graphics,
                    font,
                    x + PROGRESS_X + PROGRESS_WIDTH / 2,
                    y + PROGRESS_Y - 1,
                    menu.breedTicks(),
                    menu.maxBreedTicks()
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

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        if (!variantListOpen) {
            return;
        }
        graphics.nextStratum();
        int variantCount = menu.villagerVariantCount();
        int visible = Math.min(MAX_VISIBLE_VARIANTS, variantCount - variantScroll);
        for (int row = 0; row < visible; row++) {
            int variant = variantScroll + row;
            int rowX = leftPos + VARIANT_LIST_X;
            int rowY = topPos + VARIANT_LIST_Y + row * VARIANT_ROW_HEIGHT;
            boolean selected = variant == menu.selectedVillagerVariant();
            graphics.fill(
                    rowX,
                    rowY,
                    rowX + VARIANT_LIST_WIDTH,
                    rowY + VARIANT_ROW_HEIGHT - 1,
                    selected ? 0xF0A66D35 : 0xF04C321F
            );
            graphics.outline(
                    rowX,
                    rowY,
                    VARIANT_LIST_WIDTH,
                    VARIANT_ROW_HEIGHT - 1,
                    selected ? 0xFFFFD57A : 0xFFC49A68
            );
            graphics.centeredText(
                    font,
                    Component.translatable(menu.villagerVariantKey(variant)),
                    rowX + VARIANT_LIST_WIDTH / 2,
                    rowY + 4,
                    0xFFFFFFFF
            );
        }
        drawVariantScrollbar(graphics, variantCount);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (variantListOpen && menu.villagerVariantCount() > MAX_VISIBLE_VARIANTS) {
            int maxScroll = menu.villagerVariantCount() - MAX_VISIBLE_VARIANTS;
            variantScroll = Mth.clamp((int)(variantScroll - scrollY), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    private void toggleVariantList() {
        if (variantListOpen) {
            closeVariantList();
            return;
        }
        variantListOpen = true;
        int variantCount = menu.villagerVariantCount();
        variantScroll = Mth.clamp(
                menu.selectedVillagerVariant() - MAX_VISIBLE_VARIANTS / 2,
                0,
                Math.max(0, variantCount - MAX_VISIBLE_VARIANTS)
        );
        int visible = Math.min(MAX_VISIBLE_VARIANTS, variantCount);
        for (int row = 0; row < visible; row++) {
            VariantButton button = new VariantButton(
                    leftPos + VARIANT_LIST_X,
                    topPos + VARIANT_LIST_Y + row * VARIANT_ROW_HEIGHT,
                    row
            );
            variantButtons.add(addRenderableWidget(button));
        }
    }

    private void closeVariantList() {
        for (VariantButton button : variantButtons) {
            removeWidget(button);
        }
        variantButtons.clear();
        variantListOpen = false;
    }

    private void selectVariant(int variant) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    BreederMenu.SELECT_VARIANT_BUTTON_BASE + variant
            );
        }
        closeVariantList();
    }

    private Component variantSelectorLabel() {
        return Component.translatable("button.trading_cells.villager_skin");
    }

    private void drawVariantScrollbar(GuiGraphicsExtractor graphics, int variantCount) {
        if (variantCount <= MAX_VISIBLE_VARIANTS) {
            return;
        }
        int trackX = leftPos + VARIANT_LIST_X + VARIANT_LIST_WIDTH - 4;
        int trackY = topPos + VARIANT_LIST_Y + 2;
        int trackHeight = MAX_VISIBLE_VARIANTS * VARIANT_ROW_HEIGHT - 5;
        int thumbHeight = Math.max(9, trackHeight * MAX_VISIBLE_VARIANTS / variantCount);
        int maxScroll = variantCount - MAX_VISIBLE_VARIANTS;
        int thumbY = trackY + (trackHeight - thumbHeight) * variantScroll / maxScroll;
        graphics.fill(trackX, trackY, trackX + 2, trackY + trackHeight, 0xFF2A190E);
        graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, 0xFFFFD57A);
    }

    private final class VariantButton extends Button.Plain {
        private VariantButton(int x, int y, int row) {
            super(
                    x,
                    y,
                    VARIANT_LIST_WIDTH,
                    VARIANT_ROW_HEIGHT - 1,
                    CommonComponents.EMPTY,
                    ignored -> selectVariant(variantScroll + row),
                    DEFAULT_NARRATION
            );
        }
    }
}
