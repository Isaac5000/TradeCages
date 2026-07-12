package com.cosmocraft.trading_cells.platform.neoforge.event;

import com.cosmocraft.trading_cells.platform.neoforge.bootstrap.TradingCells;
import com.cosmocraft.trading_cells.platform.neoforge.network.ResetTradesPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MerchantMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TradingCells.MOD_ID, value = Dist.CLIENT)
public final class TradingCellClientScreenEvent {
    private static final int MERCHANT_SCREEN_WIDTH = 276;
    private static final int MERCHANT_SCREEN_HEIGHT = 166;
    private static final Identifier RESET_TRADES_SPRITE = Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, "reset_trades");
    private static final Identifier RESET_TRADES_HOVERED_SPRITE = Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, "reset_trades_hovered");
    private static final WidgetSprites RESET_TRADES_WIDGET = new WidgetSprites(RESET_TRADES_SPRITE, RESET_TRADES_HOVERED_SPRITE);

    private TradingCellClientScreenEvent() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof MerchantScreen screen)) {
            return;
        }

        int left = (screen.width - MERCHANT_SCREEN_WIDTH) / 2;
        int top = (screen.height - MERCHANT_SCREEN_HEIGHT) / 2;
        ResetTradesButton resetButton = new ResetTradesButton(screen, left + 4, top + 4);
        resetButton.setTooltip(Tooltip.create(Component.translatable("button.trading_cells.reset_trades")));
        event.addListener(resetButton);
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_C
                || !(event.getScreen() instanceof MerchantScreen screen)
                || !canResetTrades(screen.getMenu())) {
            return;
        }
        sendResetTradesPacket();
        event.setCanceled(true);
    }

    private static void sendResetTradesPacket() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.getConnection().send(ResetTradesPayload.INSTANCE.toVanillaServerbound());
        }
    }

    private static boolean canResetTrades(MerchantMenu menu) {
        return menu.getTraderLevel() <= 1 && menu.getTraderXp() <= 0 && !menu.getOffers().isEmpty();
    }

    private static final class ResetTradesButton extends ImageButton {
        private final MerchantScreen screen;

        private ResetTradesButton(MerchantScreen screen, int x, int y) {
            super(x, y, 12, 12, RESET_TRADES_WIDGET, button -> sendResetTradesPacket(), Component.empty());
            this.screen = screen;
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            this.active = canResetTrades(screen.getMenu());
            if (!this.active) {
                return;
            }
            super.extractContents(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        protected void handleCursor(GuiGraphicsExtractor graphics) {
            if (this.active) {
                super.handleCursor(graphics);
            }
        }
    }
}
