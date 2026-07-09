package com.example.examplemod.feature.milkcookie.adapters.output;

import com.example.examplemod.feature.milkcookie.domain.model.MilkCookieFoodSpec;
import com.example.examplemod.platform.neoforge.bootstrap.TradingCells;
import com.example.examplemod.platform.neoforge.registration.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.neoforged.neoforge.registries.DeferredItem;

@SuppressWarnings("java:S1118")
public final class MilkCookieRegistrationAdapter {
    public static final String COOKIE_WITH_MILK_ID = "cookie_with_milk";
    public static final MilkCookieFoodSpec FOOD_SPEC = MilkCookieFoodSpec.standard();

    public static final DeferredItem<Item> COOKIE_WITH_MILK_ITEM =
            Registration.ITEMS.register(COOKIE_WITH_MILK_ID, () -> new Item(
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, COOKIE_WITH_MILK_ID)))
                            .stacksTo(64)
                            .food(
                                    new FoodProperties.Builder()
                                            .nutrition(FOOD_SPEC.nutrition())
                                            .saturationModifier(FOOD_SPEC.saturationModifier())
                                            .build(),
                                    Consumable.builder()
                                            .onConsume(new ApplyStatusEffectsConsumeEffect(
                                                    new MobEffectInstance(MobEffects.NAUSEA, FOOD_SPEC.nauseaDurationTicks(), 0),
                                                    1.0F
                                            ))
                                            .onConsume(new ApplyStatusEffectsConsumeEffect(
                                                    new MobEffectInstance(MobEffects.HUNGER, FOOD_SPEC.hungerDurationTicks(), 0),
                                                    FOOD_SPEC.hungerChance()
                                            ))
                                            .build()
                            )
            ));

    private MilkCookieRegistrationAdapter() {
    }

    public static void load() {
        // Forces class loading so all DeferredRegister entries are created.
    }
}
