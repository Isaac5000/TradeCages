package com.cosmocraft.trading_cells.platform.neoforge.event;

import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.PiglinCapturerItem;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.VillagerTradingCellBlock;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.output.TradingCellsRegistrationAdapter;
import com.cosmocraft.trading_cells.platform.neoforge.bootstrap.TradingCells;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = TradingCells.MOD_ID)
public final class NeoForgeEventAdapter {
    private NeoForgeEventAdapter() {
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
            return;
        }

        ItemStack captureTarget = createSingleCapturerTarget(heldItem);
        if (!VillagerCapturerItem.captureVillager(captureTarget, villager)) {
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        finishStackedCapture(event.getEntity(), heldItem, captureTarget);
        villager.discard();
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
            return;
        }

        ItemStack captureTarget = createSingleCapturerTarget(heldItem);
        if (!PiglinCapturerItem.capturePiglin(captureTarget, piglin)) {
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        finishStackedCapture(event.getEntity(), heldItem, captureTarget);
        piglin.discard();
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
