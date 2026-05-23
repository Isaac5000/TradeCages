package com.example.examplemod.platform.neoforge.event;
import com.example.examplemod.feature.tradecages.adapters.input.TradeCageBlock;
import com.example.examplemod.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.output.TradeCageRegistrationAdapter;
import com.example.examplemod.platform.neoforge.bootstrap.ExampleMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
@EventBusSubscriber(modid = ExampleMod.MOD_ID)
public final class NeoForgeEventAdapter {
    private NeoForgeEventAdapter() {
    }
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ExampleMod.LOGGER.info("Server starting (Trade Cages)");
    }
    @SubscribeEvent
    public static void onBlockEvent(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedBlock().getBlock() instanceof TradeCageBlock) {
            ExampleMod.LOGGER.info("Trade Cage placed at {}", event.getPos());
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
        if (!heldItem.is(TradeCageRegistrationAdapter.VILLAGER_CAPTURER_ITEM.get())) {
            return;
        }
        if (VillagerCapturerItem.hasCapturedVillager(heldItem)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
        if (!VillagerCapturerItem.captureVillager(heldItem, villager)) {
            return;
        }
        villager.discard();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
