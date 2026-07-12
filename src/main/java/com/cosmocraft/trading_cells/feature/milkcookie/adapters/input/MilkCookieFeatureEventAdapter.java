package com.cosmocraft.trading_cells.feature.milkcookie.adapters.input;

import com.cosmocraft.trading_cells.feature.milkcookie.adapters.output.MilkCookieRegistrationAdapter;
import com.cosmocraft.trading_cells.feature.milkcookie.application.port.input.CreateMilkCookieUseCase;
import com.cosmocraft.trading_cells.feature.milkcookie.application.usecase.CreateMilkCookieUseCaseImp;
import com.cosmocraft.trading_cells.feature.milkcookie.domain.model.MilkCookieCreationRequest;
import com.cosmocraft.trading_cells.platform.neoforge.bootstrap.TradingCells;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = TradingCells.MOD_ID)
public final class MilkCookieFeatureEventAdapter {
    private static final CreateMilkCookieUseCase CREATE_MILK_COOKIE = new CreateMilkCookieUseCaseImp();

    private MilkCookieFeatureEventAdapter() {
    }

    @SubscribeEvent
    public static void onAdultHorseInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(event.getHand());
        Entity target = event.getTarget();

        MilkCookieCreationRequest request = new MilkCookieCreationRequest(
                heldItem.is(Items.COOKIE),
                isAdultHorse(target)
        );

        if (!CREATE_MILK_COOKIE.canCreate(request)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        ItemStack createdItem = new ItemStack(MilkCookieRegistrationAdapter.COOKIE_WITH_MILK_ITEM.get());
        if (!player.getAbilities().instabuild) {
            heldItem.shrink(1);
        }

        if (!player.getInventory().add(createdItem)) {
            player.drop(createdItem, false);
        }

    }

    private static boolean isAdultHorse(Entity target) {
        return target instanceof Horse horse && !horse.isBaby();
    }
}
