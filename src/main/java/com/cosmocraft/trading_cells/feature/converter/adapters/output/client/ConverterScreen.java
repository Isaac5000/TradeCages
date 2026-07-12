package com.cosmocraft.trading_cells.feature.converter.adapters.output.client;

import com.cosmocraft.trading_cells.feature.converter.adapters.input.ConverterBlockEntity;
import com.cosmocraft.trading_cells.feature.converter.adapters.input.ConverterMenu;
import com.cosmocraft.trading_cells.feature.converter.domain.model.ConverterStage;
import com.cosmocraft.trading_cells.platform.neoforge.client.screen.MachineScreenLayout;
import com.cosmocraft.trading_cells.platform.neoforge.client.screen.MachineScreenUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class ConverterScreen extends AbstractContainerScreen<ConverterMenu> {
    private static final Identifier MOSSY_COBBLESTONE = Identifier.fromNamespaceAndPath(
            "minecraft",
            "textures/block/mossy_cobblestone.png"
    );
    private static final int PROGRESS_X = 54;
    private static final int PROGRESS_Y = 99;
    private static final int PROGRESS_WIDTH = 68;
    private static final int PROGRESS_HEIGHT = 8;

    public ConverterScreen(ConverterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, MachineScreenLayout.WIDTH, MachineScreenLayout.HEIGHT);
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
        MachineScreenLayout.drawBackground(graphics, x, y, MOSSY_COBBLESTONE);
        for (int index = 0; index < ConverterBlockEntity.POTION_SLOT_COUNT; index++) {
            MachineScreenLayout.drawSlot(graphics, x, y, 43 + index * 24, 35);
        }
        for (int index = 0; index < ConverterBlockEntity.APPLE_SLOT_COUNT; index++) {
            MachineScreenLayout.drawSlot(graphics, x, y, 43 + index * 24, 63);
        }
        MachineScreenLayout.drawProgressFrame(graphics, x + 53, y + 96);

        ConverterStage stage = menu.stage();
        graphics.centeredText(font, Component.translatable(stageKey(stage)), x + 88, y + 84, 0x303030);
        if (stage.isProcessing()) {
            int progress = Math.min(PROGRESS_WIDTH, menu.stageTicks() * PROGRESS_WIDTH / menu.maxStageTicks());
            if (progress > 0) {
                graphics.fill(x + PROGRESS_X, y + PROGRESS_Y, x + PROGRESS_X + progress, y + PROGRESS_Y + PROGRESS_HEIGHT, 0xFF8E5AA7);
            }
            MachineScreenUtil.drawCenteredCountdown(
                    graphics,
                    font,
                    x + PROGRESS_X + PROGRESS_WIDTH / 2,
                    y + PROGRESS_Y - 1,
                    menu.stageTicks(),
                    menu.maxStageTicks()
            );
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0x303030, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x303030, false);
    }

    private static String stageKey(ConverterStage stage) {
        return switch (stage) {
            case IDLE -> "label.trading_cells.converter_ready";
            case INFECTING -> "label.trading_cells.converter_infecting";
            case CURING -> "label.trading_cells.converter_curing";
        };
    }
}
