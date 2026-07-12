package com.cosmocraft.trading_cells.feature.ironfarm.adapters.output.client;

import com.cosmocraft.trading_cells.feature.ironfarm.adapters.input.IronFarmBlockEntity;
import com.cosmocraft.trading_cells.feature.ironfarm.adapters.input.IronFarmMenu;
import com.cosmocraft.trading_cells.platform.neoforge.client.screen.MachineScreenLayout;
import com.cosmocraft.trading_cells.platform.neoforge.client.screen.MachineScreenUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class IronFarmScreen extends AbstractContainerScreen<IronFarmMenu> {
    private static final Identifier COBBLESTONE = Identifier.fromNamespaceAndPath("minecraft", "textures/block/cobblestone.png");
    private static final int PROGRESS_X = 54;
    private static final int PROGRESS_Y = 51;
    private static final int PROGRESS_WIDTH = 68;
    private static final int PROGRESS_HEIGHT = 8;
    private Checkbox flowersCheckbox;

    public IronFarmScreen(IronFarmMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, MachineScreenLayout.WIDTH, MachineScreenLayout.HEIGHT);
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 129;
    }

    @Override
    protected void init() {
        super.init();
        rebuildFlowersCheckbox();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (flowersCheckbox != null && flowersCheckbox.selected() != menu.flowersEnabled()) {
            rebuildFlowersCheckbox();
        }
    }

    private void rebuildFlowersCheckbox() {
        if (flowersCheckbox != null) {
            removeWidget(flowersCheckbox);
        }
        flowersCheckbox = addRenderableWidget(Checkbox.builder(Component.translatable("label.trading_cells.flowers"), font)
                .pos(leftPos + 8, topPos + 104)
                .maxWidth(74)
                .selected(menu.flowersEnabled())
                .onValueChange((checkbox, selected) -> setFlowersEnabled(selected))
                .build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        MachineScreenLayout.drawBackground(graphics, x, y, COBBLESTONE);
        for (int index = 0; index < IronFarmBlockEntity.VILLAGER_SLOT_COUNT; index++) {
            MachineScreenLayout.drawSlot(
                    graphics,
                    x,
                    y,
                    IronFarmMenu.VILLAGER_ROW_X + index * 24,
                    IronFarmMenu.VILLAGER_ROW_Y
            );
        }
        for (int index = 0; index < IronFarmBlockEntity.OUTPUT_SLOT_COUNT; index++) {
            MachineScreenLayout.drawSlot(
                    graphics,
                    x,
                    y,
                    IronFarmMenu.OUTPUT_ROW_X + index * 24,
                    IronFarmMenu.OUTPUT_ROW_Y
            );
        }
        MachineScreenLayout.drawProgressFrame(graphics, x + 53, y + 48);
        drawEfficiencyInfo(graphics, x + 88, y + 99);

        int progress = Math.min(PROGRESS_WIDTH, menu.cycleTicks() * PROGRESS_WIDTH / menu.maxCycleTicks());
        if (progress > 0) {
            graphics.fill(x + PROGRESS_X, y + PROGRESS_Y, x + PROGRESS_X + progress, y + PROGRESS_Y + PROGRESS_HEIGHT, 0xFFD7D7D7);
        }
        if (menu.cycleTicks() > 0) {
            MachineScreenUtil.drawCenteredCountdown(
                    graphics,
                    font,
                    x + PROGRESS_X + PROGRESS_WIDTH / 2,
                    y + PROGRESS_Y - 1,
                    menu.cycleTicks(),
                    menu.maxCycleTicks()
            );
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0x303030, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x303030, false);
    }

    private void drawEfficiencyInfo(GuiGraphicsExtractor graphics, int x, int y) {
        int villagers = menu.villagerCount();
        int maximum = menu.maximumVillagers();
        int multiplier = menu.currentMultiplier();
        graphics.text(
                font,
                Component.translatable("label.trading_cells.iron_villagers", villagers, maximum),
                x,
                y,
                0x303030,
                false
        );
        graphics.text(
                font,
                Component.translatable("label.trading_cells.iron_current_efficiency", multiplier),
                x,
                y + 10,
                0x303030,
                false
        );
        Component next = villagers >= maximum
                ? Component.translatable("label.trading_cells.iron_maximum")
                : Component.translatable(
                        "label.trading_cells.iron_next_efficiency",
                        Math.max(0, menu.nextMultiplier() - multiplier)
                );
        graphics.text(font, next, x, y + 20, 0x303030, false);
    }

    private void setFlowersEnabled(boolean enabled) {
        menu.setClientFlowersEnabled(enabled);
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    enabled ? IronFarmMenu.ENABLE_FLOWERS_BUTTON : IronFarmMenu.DISABLE_FLOWERS_BUTTON
            );
        }
    }
}
