package com.cosmocraft.trading_cells.feature.incubators.adapters.input;

import com.cosmocraft.trading_cells.feature.incubators.domain.model.IncubatorKind;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.PiglinCapturerItem;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.cosmocraft.trading_cells.feature.tradecages.adapters.output.TradingCellsRegistrationAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class CapturedMobStackAdapter {
    private CapturedMobStackAdapter() {
    }

    public static boolean isBaby(IncubatorKind kind, ItemStack stack) {
        CompoundTag data = copyData(kind, stack);
        if (data == null) {
            return false;
        }
        return kind == IncubatorKind.VILLAGER
                ? VillagerCapturerItem.isBabyVillager(data)
                : PiglinCapturerItem.isBabyPiglin(data);
    }

    public static boolean isFilledCapturer(IncubatorKind kind, ItemStack stack) {
        if (stack.isEmpty() || !stack.is(capturerItem(kind))) {
            return false;
        }
        return kind == IncubatorKind.VILLAGER
                ? VillagerCapturerItem.hasCapturedVillager(stack)
                : PiglinCapturerItem.hasCapturedPiglin(stack);
    }

    public static ItemStack mature(IncubatorKind kind, ItemStack source) {
        CompoundTag adultData = copyData(kind, source);
        if (adultData == null) {
            return ItemStack.EMPTY;
        }

        if (kind == IncubatorKind.VILLAGER) {
            adultData.putInt("Age", 0);
            adultData.putInt("ForcedAge", 0);
            adultData.remove("AgeLocked");
        } else {
            adultData.putBoolean("IsBaby", false);
            if (adultData.contains("Age")) {
                adultData.putInt("Age", 0);
            }
        }

        ItemStack adult = source.copy();
        adult.setCount(1);
        setData(kind, adult, adultData);
        return adult;
    }

    public static @Nullable Entity createEntity(IncubatorKind kind, Level level, ItemStack stack, BlockPos pos) {
        CompoundTag data = copyData(kind, stack);
        if (data == null) {
            return null;
        }
        return kind == IncubatorKind.VILLAGER
                ? VillagerCapturerItem.createCapturedVillager(level, data, pos)
                : PiglinCapturerItem.createCapturedPiglin(level, data, pos);
    }

    public static Item capturerItem(IncubatorKind kind) {
        return kind == IncubatorKind.VILLAGER
                ? TradingCellsRegistrationAdapter.VILLAGER_CAPTURER_ITEM.get()
                : TradingCellsRegistrationAdapter.PIGLIN_CAPTURER_ITEM.get();
    }

    private static @Nullable CompoundTag copyData(IncubatorKind kind, ItemStack stack) {
        if (!isFilledCapturer(kind, stack)) {
            return null;
        }
        return kind == IncubatorKind.VILLAGER
                ? VillagerCapturerItem.getCapturedVillagerData(stack)
                : PiglinCapturerItem.getCapturedPiglinData(stack);
    }

    private static void setData(IncubatorKind kind, ItemStack stack, CompoundTag data) {
        if (kind == IncubatorKind.VILLAGER) {
            VillagerCapturerItem.setCapturedVillagerData(stack, data);
        } else {
            PiglinCapturerItem.setCapturedPiglinData(stack, data);
        }
    }
}
