package com.cosmocraft.trading_cells.feature.autotrader.adapters.output.client;

import com.cosmocraft.trading_cells.feature.autotrader.adapters.input.AutotraderMenu;
import com.cosmocraft.trading_cells.feature.autotrader.domain.model.AutotraderPolicy;
import com.cosmocraft.trading_cells.platform.neoforge.client.screen.MachineScreenLayout;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public final class AutotraderScreen extends AbstractContainerScreen<AutotraderMenu> {
    private static final Identifier EMERALD_BLOCK = Identifier.fromNamespaceAndPath("minecraft", "textures/block/emerald_block.png");
    private static final Identifier TRADE_ARROW = Identifier.withDefaultNamespace("container/villager/trade_arrow");
    private static final Identifier TRADE_ARROW_OUT_OF_STOCK = Identifier.withDefaultNamespace("container/villager/trade_arrow_out_of_stock");
    private static final int MAX_VISIBLE_OFFERS = 3;
    private static final int OFFER_LIST_X = 8;
    private static final int OFFER_LIST_Y = 36;
    private static final int OFFER_LIST_WIDTH = 160;
    private static final int OFFER_ROW_HEIGHT = 20;
    private final List<OfferButton> offerButtons = new ArrayList<>();
    private Button selectorButton;
    private Button experienceButton;
    private boolean offerListOpen;
    private int offerScroll;
    private int listedOfferCount;

    public AutotraderScreen(AutotraderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, MachineScreenLayout.WIDTH, MachineScreenLayout.HEIGHT);
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 126;
    }

    @Override
    protected void init() {
        super.init();
        selectorButton = addRenderableWidget(Button.builder(selectorLabel(), button -> toggleOfferList())
                .bounds(leftPos + 18, topPos + 16, 140, 18)
                .build());
        experienceButton = addRenderableWidget(Button.builder(experienceLabel(), button -> extractExperience())
                .bounds(leftPos + 8, topPos + 108, 160, 18)
                .build());
        refreshSelector();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshSelector();
        if (offerListOpen) {
            int offerCount = menu.offers().size();
            if (!menu.hasVillager()) {
                closeOfferList();
            } else if (offerCount != listedOfferCount) {
                rebuildOfferButtons();
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        MachineScreenLayout.drawSingleTextureBackground(graphics, x, y, EMERALD_BLOCK);

        for (int index = 0; index < AutotraderPolicy.INPUT_SLOTS_PER_COST; index++) {
            MachineScreenLayout.drawSlot(
                    graphics,
                    x,
                    y,
                    AutotraderMenu.INPUT_ROW_X + index * 24,
                    AutotraderMenu.INPUT_A_ROW_Y
            );
            MachineScreenLayout.drawSlot(
                    graphics,
                    x,
                    y,
                    AutotraderMenu.INPUT_ROW_X + index * 24,
                    AutotraderMenu.INPUT_B_ROW_Y
            );
        }
        for (int index = 0; index < AutotraderPolicy.OUTPUT_SLOTS; index++) {
            MachineScreenLayout.drawSlot(
                    graphics,
                    x,
                    y,
                    AutotraderMenu.INPUT_ROW_X + index * 24,
                    AutotraderMenu.OUTPUT_ROW_Y
            );
        }

        graphics.text(font, Component.literal("< 1"), x + 14, y + 41, 0xF1F1F1, true);
        graphics.text(font, Component.literal("2 >"), x + 149, y + 65, 0xF1F1F1, true);
        graphics.centeredText(font, Component.literal("v"), x + 29, y + 89, 0xF1F1F1);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0xF1F1F1, true);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x303030, false);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        if (!offerListOpen) {
            return;
        }
        graphics.nextStratum();
        List<MerchantOffer> offers = menu.offers();
        int visible = Math.min(MAX_VISIBLE_OFFERS, offers.size() - offerScroll);
        if (visible <= 0) {
            return;
        }
        int listX = leftPos + OFFER_LIST_X;
        int listY = topPos + OFFER_LIST_Y;
        int listHeight = visible * OFFER_ROW_HEIGHT;
        graphics.fill(listX - 1, listY - 1, listX + OFFER_LIST_WIDTH + 1, listY + listHeight + 1, 0xFF0C251A);
        graphics.enableScissor(listX, listY, listX + OFFER_LIST_WIDTH, listY + listHeight);
        for (int row = 0; row < visible; row++) {
            int offerIndex = offerScroll + row;
            MerchantOffer offer = offers.get(offerIndex);
            int rowX = listX;
            int rowY = listY + row * OFFER_ROW_HEIGHT;
            int rowColor = offerIndex == menu.selectedOfferIndex() ? 0xFF3E744A : 0xFF173F2D;
            graphics.fill(rowX, rowY, rowX + 160, rowY + 19, rowColor);
            graphics.outline(rowX, rowY, 160, 19, offerIndex == menu.selectedOfferIndex() ? 0xFFF2D36A : 0xFF82A98C);

            ItemStack first = offer.getCostA();
            ItemStack second = offer.getCostB();
            ItemStack result = offer.getResult();
            graphics.fakeItem(first, rowX + 7, rowY + 1);
            graphics.itemDecorations(font, first, rowX + 7, rowY + 1);
            if (!second.isEmpty()) {
                graphics.fakeItem(second, rowX + 39, rowY + 1);
                graphics.itemDecorations(font, second, rowX + 39, rowY + 1);
            }
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    offer.isOutOfStock() ? TRADE_ARROW_OUT_OF_STOCK : TRADE_ARROW,
                    rowX + 70,
                    rowY + 5,
                    10,
                    9
            );
            graphics.fakeItem(result, rowX + 105, rowY + 1);
            graphics.itemDecorations(font, result, rowX + 105, rowY + 1);
            if (mouseY >= rowY && mouseY < rowY + 19) {
                if (mouseX >= rowX + 7 && mouseX < rowX + 25) {
                    graphics.setTooltipForNextFrame(font, first, mouseX, mouseY);
                } else if (!second.isEmpty() && mouseX >= rowX + 39 && mouseX < rowX + 57) {
                    graphics.setTooltipForNextFrame(font, second, mouseX, mouseY);
                } else if (mouseX >= rowX + 105 && mouseX < rowX + 123) {
                    graphics.setTooltipForNextFrame(font, result, mouseX, mouseY);
                }
            }
        }
        graphics.disableScissor();
        drawOfferScrollbar(graphics, offers.size());
        if (offerScroll > 0) {
            graphics.centeredText(font, Component.literal("^"), listX + OFFER_LIST_WIDTH - 7, listY + 1, 0xFFF2D36A);
        }
        if (offerScroll + visible < offers.size()) {
            graphics.centeredText(
                    font,
                    Component.literal("v"),
                    listX + OFFER_LIST_WIDTH - 7,
                    listY + listHeight - 9,
                    0xFFF2D36A
            );
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (offerListOpen && isInsideOfferList(x, y)) {
            int maxScroll = Math.max(0, menu.offers().size() - MAX_VISIBLE_OFFERS);
            offerScroll = Mth.clamp((int) (offerScroll - scrollY), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    private void toggleOfferList() {
        if (offerListOpen) {
            closeOfferList();
            return;
        }
        List<MerchantOffer> offers = menu.offers();
        if (offers.isEmpty()) {
            return;
        }
        offerListOpen = true;
        offerScroll = Mth.clamp(
                menu.selectedOfferIndex() - MAX_VISIBLE_OFFERS / 2,
                0,
                Math.max(0, offers.size() - MAX_VISIBLE_OFFERS)
        );
        rebuildOfferButtons();
    }

    private void rebuildOfferButtons() {
        for (OfferButton button : offerButtons) {
            removeWidget(button);
        }
        offerButtons.clear();
        int offerCount = menu.offers().size();
        listedOfferCount = offerCount;
        offerScroll = Mth.clamp(offerScroll, 0, Math.max(0, offerCount - MAX_VISIBLE_OFFERS));
        int visibleOffers = Math.min(MAX_VISIBLE_OFFERS, offerCount);
        for (int row = 0; row < visibleOffers; row++) {
            OfferButton button = new OfferButton(
                    leftPos + OFFER_LIST_X,
                    topPos + OFFER_LIST_Y + row * OFFER_ROW_HEIGHT,
                    row
            );
            offerButtons.add(addRenderableWidget(button));
        }
    }

    private void closeOfferList() {
        for (OfferButton button : offerButtons) {
            removeWidget(button);
        }
        offerButtons.clear();
        offerListOpen = false;
        listedOfferCount = 0;
    }

    private void selectOffer(int offerIndex) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    AutotraderMenu.SELECT_OFFER_BUTTON_BASE + offerIndex
            );
        }
        closeOfferList();
    }

    private void extractExperience() {
        if (minecraft != null && minecraft.gameMode != null && menu.storedExperience() > 0) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    AutotraderMenu.EXTRACT_EXPERIENCE_BUTTON
            );
        }
    }

    private void refreshSelector() {
        if (selectorButton == null) {
            return;
        }
        boolean hasOffers = !menu.offers().isEmpty();
        selectorButton.visible = menu.hasVillager();
        selectorButton.active = hasOffers;
        selectorButton.setMessage(selectorLabel());
        if (experienceButton != null) {
            experienceButton.active = menu.storedExperience() > 0;
            experienceButton.setMessage(experienceLabel());
        }
    }

    private Component selectorLabel() {
        MerchantOffer selected = menu.selectedOffer();
        if (selected == null) {
            return Component.translatable("label.trading_cells.no_selected_trade");
        }
        return Component.translatable(
                "button.trading_cells.selected_trade",
                menu.selectedOfferIndex() + 1,
                menu.offers().size()
        );
    }

    private Component experienceLabel() {
        return Component.translatable(
                "button.trading_cells.extract_xp",
                menu.storedExperience(),
                experienceLevels(menu.storedExperience())
        );
    }

    private static int experienceLevels(int experience) {
        int low = 0;
        int high = 1;
        while (totalExperienceAtLevel(high) <= experience && high < 65_536) {
            high *= 2;
        }
        while (low + 1 < high) {
            int middle = low + (high - low) / 2;
            if (totalExperienceAtLevel(middle) <= experience) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private static long totalExperienceAtLevel(int level) {
        long value = level;
        if (level <= 16) {
            return value * value + 6L * value;
        }
        if (level <= 31) {
            return (5L * value * value - 81L * value + 720L) / 2L;
        }
        return (9L * value * value - 325L * value + 4_440L) / 2L;
    }

    private void drawOfferScrollbar(GuiGraphicsExtractor graphics, int offerCount) {
        if (offerCount <= MAX_VISIBLE_OFFERS) {
            return;
        }
        int trackX = leftPos + OFFER_LIST_X + OFFER_LIST_WIDTH - 3;
        int trackY = topPos + OFFER_LIST_Y + 2;
        int trackHeight = MAX_VISIBLE_OFFERS * OFFER_ROW_HEIGHT - 4;
        int thumbHeight = Math.max(10, trackHeight * MAX_VISIBLE_OFFERS / offerCount);
        int maxScroll = offerCount - MAX_VISIBLE_OFFERS;
        int thumbY = trackY + (trackHeight - thumbHeight) * offerScroll / maxScroll;
        graphics.fill(trackX, trackY, trackX + 2, trackY + trackHeight, 0xFF0C251A);
        graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, 0xFFE6D06B);
    }

    private boolean isInsideOfferList(double mouseX, double mouseY) {
        int visible = Math.min(MAX_VISIBLE_OFFERS, menu.offers().size());
        return visible > 0
                && mouseX >= leftPos + OFFER_LIST_X
                && mouseX < leftPos + OFFER_LIST_X + OFFER_LIST_WIDTH
                && mouseY >= topPos + OFFER_LIST_Y
                && mouseY < topPos + OFFER_LIST_Y + visible * OFFER_ROW_HEIGHT;
    }

    private final class OfferButton extends Button.Plain {
        private OfferButton(int x, int y, int row) {
            super(
                    x,
                    y,
                    OFFER_LIST_WIDTH,
                    OFFER_ROW_HEIGHT,
                    CommonComponents.EMPTY,
                    ignored -> selectOffer(offerScroll + row),
                    DEFAULT_NARRATION
            );
        }
    }
}
