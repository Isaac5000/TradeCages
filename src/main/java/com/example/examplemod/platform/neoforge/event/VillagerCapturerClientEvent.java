package com.example.examplemod.platform.neoforge.event;

import com.example.examplemod.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.example.examplemod.feature.tradecages.adapters.output.client.VillagerCapturerItemRenderSupport;
import com.example.examplemod.feature.tradecages.adapters.output.TradeCageRegistrationAdapter;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.minecraft.client.Minecraft;

public final class VillagerCapturerClientEvent {
    private VillagerCapturerClientEvent() {
    }

    public static void onRenderHand(RenderHandEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(TradeCageRegistrationAdapter.VILLAGER_CAPTURER_ITEM.get())) {
            return;
        }
        if (!VillagerCapturerItem.hasCapturedVillager(stack)) {
            return;
        }

        if (VillagerCapturerItemRenderSupport.renderVillager(stack, event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(), event.getPackedLight(), event.getPartialTick())) {
            event.setCanceled(true);
        }
    }
}


