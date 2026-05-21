package com.example.examplemod.platform.neoforge.event;
import com.example.examplemod.feature.tradecages.adapters.input.TradeCageBlock;
import com.example.examplemod.feature.tradecages.adapters.output.TradeCageRegistrationAdapter;
import com.example.examplemod.platform.neoforge.bootstrap.ExampleMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
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
        if (!TradeCageRegistrationAdapter.CAPTURE_VILLAGER_USE_CASE.hasCapacity()) {
            event.getEntity().sendSystemMessage(Component.literal("La caja esta llena."));
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
        Inventory inventory = event.getEntity().getInventory();
        int freeSlot = inventory.getFreeSlot();
        if (freeSlot < 0) {
            event.getEntity().sendSystemMessage(Component.literal("No tienes espacio en el inventario."));
            return;
        }
        if (!consumeOneTradeCage(inventory)) {
            event.getEntity().sendSystemMessage(Component.literal("Necesitas una Trade Cage vacia."));
            return;
        }
        boolean captured = TradeCageRegistrationAdapter.CAPTURE_VILLAGER_USE_CASE.captureVillager(villager);
        if (!captured) {
            return;
        }
        villager.discard();
        ItemStack capturedStack = new ItemStack(TradeCageRegistrationAdapter.CAPTURED_VILLAGER_ITEM.get());
        inventory.add(capturedStack);
        event.getEntity().sendSystemMessage(Component.literal("Aldeano capturado."));
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
    private static boolean consumeOneTradeCage(Inventory inventory) {
        // Use public Inventory API instead of accessing the private `items` field.
        // Iterate over the main inventory slots (0..35).
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.is(TradeCageRegistrationAdapter.TRADE_CAGE_ITEM.get()) && stack.getCount() > 0) {
                // Remove one from the slot using the Inventory API to ensure internal state stays consistent.
                inventory.removeItem(i, 1);
                return true;
            }
        }
        return false;
    }
}
