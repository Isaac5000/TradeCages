package com.cosmocraft.trading_cells.feature.farmer.adapters.output.client;

import com.cosmocraft.trading_cells.feature.farmer.adapters.input.FarmerMenu;
import com.cosmocraft.trading_cells.platform.neoforge.client.screen.MachineScreenLayout;
import com.cosmocraft.trading_cells.platform.neoforge.client.screen.MachineScreenUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class FarmerScreen extends AbstractContainerScreen<FarmerMenu> {
    private static final Identifier DIRT = Identifier.fromNamespaceAndPath("minecraft", "textures/block/dirt.png");
    private static final int PROGRESS_X = 54;
    private static final int PROGRESS_Y = 70;
    private static final int PROGRESS_WIDTH = 68;
    private static final int PROGRESS_HEIGHT = 8;

    public FarmerScreen(FarmerMenu menu, Inventory inventory, Component title) {
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
        MachineScreenLayout.drawBackground(graphics, x, y, DIRT);

        MachineScreenLayout.drawSlot(graphics, x, y, 43, 30);
        MachineScreenLayout.drawSlot(graphics, x, y, 73, 30);
        MachineScreenLayout.drawSlot(graphics, x, y, 103, 30);
        for (int index = 0; index < 4; index++) {
            MachineScreenLayout.drawSlot(graphics, x, y, 43 + index * 24, 94);
        }
        MachineScreenLayout.drawProgressFrame(graphics, x + 53, y + 67);

        int progress = Math.min(PROGRESS_WIDTH, menu.growthTicks() * PROGRESS_WIDTH / menu.maxGrowthTicks());
        if (progress > 0) {
            graphics.fill(x + PROGRESS_X, y + PROGRESS_Y, x + PROGRESS_X + progress, y + PROGRESS_Y + PROGRESS_HEIGHT, 0xFF55A630);
        }
        MachineScreenUtil.drawCenteredCountdown(
                graphics,
                font,
                x + PROGRESS_X + PROGRESS_WIDTH / 2,
                y + PROGRESS_Y - 1,
                menu.growthTicks(),
                menu.maxGrowthTicks()
        );
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0x303030, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x303030, false);
    }
}
