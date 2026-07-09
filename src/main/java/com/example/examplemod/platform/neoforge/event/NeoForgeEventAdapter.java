package com.example.examplemod.platform.neoforge.event;

import com.example.examplemod.feature.tradecages.adapters.input.PiglinCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerTradingCellBlock;
import com.example.examplemod.feature.tradecages.adapters.output.TradingCellsRegistrationAdapter;
import com.example.examplemod.platform.neoforge.bootstrap.TradingCells;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@EventBusSubscriber(modid = TradingCells.MOD_ID)
public final class NeoForgeEventAdapter {
    private NeoForgeEventAdapter() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        TradingCells.LOGGER.info("Server starting (Trading Cells)");
    }

    @SubscribeEvent
    public static void onBlockEvent(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedBlock().getBlock() instanceof VillagerTradingCellBlock) {
            TradingCells.LOGGER.debug("Trading Cell placed at {}", event.getPos());
        }
    }

    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getTarget() instanceof Villager villager)) {
            return;
        }
        if (!event.getEntity().isShiftKeyDown()) {
            return;
        }

        ItemStack heldItem = event.getEntity().getItemInHand(event.getHand());
        if (!heldItem.is(TradingCellsRegistrationAdapter.VILLAGER_CAPTURER_ITEM.get())) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (VillagerCapturerItem.hasCapturedVillager(heldItem)) {
            event.getEntity().sendSystemMessage(Component.translatable("message.trading_cells.capturer_occupied"));
            return;
        }

        ItemStack captureTarget = createSingleCapturerTarget(heldItem);
        if (!VillagerCapturerItem.captureVillager(captureTarget, villager)) {
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        finishStackedCapture(event.getEntity(), heldItem, captureTarget);
        villager.discard();
        event.getEntity().sendSystemMessage(Component.translatable("message.trading_cells.villager_captured"));
    }

    @SubscribeEvent
    public static void onPiglinInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getTarget() instanceof Piglin piglin)) {
            return;
        }
        if (!event.getEntity().isShiftKeyDown()) {
            return;
        }

        ItemStack heldItem = event.getEntity().getItemInHand(event.getHand());
        if (!heldItem.is(TradingCellsRegistrationAdapter.PIGLIN_CAPTURER_ITEM.get())) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (PiglinCapturerItem.hasCapturedPiglin(heldItem)) {
            event.getEntity().sendSystemMessage(Component.translatable("message.trading_cells.capturer_occupied"));
            return;
        }

        ItemStack captureTarget = createSingleCapturerTarget(heldItem);
        if (!PiglinCapturerItem.capturePiglin(captureTarget, piglin)) {
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        finishStackedCapture(event.getEntity(), heldItem, captureTarget);
        piglin.discard();
        event.getEntity().sendSystemMessage(Component.translatable("message.trading_cells.piglin_captured"));
    }
    private static ItemStack createSingleCapturerTarget(ItemStack heldItem) {
        if (heldItem.getCount() <= 1) {
            return heldItem;
        }
        return new ItemStack(heldItem.getItem());
    }

    private static void finishStackedCapture(Player player, ItemStack heldItem, ItemStack captureTarget) {
        if (captureTarget == heldItem) {
            return;
        }

        heldItem.shrink(1);
        if (!player.getInventory().add(captureTarget)) {
            player.drop(captureTarget, false);
        }
    }
}
