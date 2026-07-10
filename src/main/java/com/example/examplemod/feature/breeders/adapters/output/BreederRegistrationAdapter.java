package com.example.examplemod.feature.breeders.adapters.output;

import com.example.examplemod.feature.breeders.adapters.input.BreederMenu;
import com.example.examplemod.feature.breeders.adapters.input.PiglinBreederBlock;
import com.example.examplemod.feature.breeders.adapters.input.PiglinBreederBlockEntity;
import com.example.examplemod.feature.breeders.adapters.input.VillagerBreederBlock;
import com.example.examplemod.feature.breeders.adapters.input.VillagerBreederBlockEntity;
import com.example.examplemod.feature.breeders.application.port.input.TickBreederUseCase;
import com.example.examplemod.feature.breeders.application.usecase.TickBreederUseCaseImp;
import com.example.examplemod.platform.neoforge.bootstrap.TradingCells;
import com.example.examplemod.platform.neoforge.registration.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

@SuppressWarnings("java:S1118")
public final class BreederRegistrationAdapter {
    public static final TickBreederUseCase TICK_BREEDER_USE_CASE = new TickBreederUseCaseImp();

    public static final String VILLAGER_BREEDER_ID = "villager_breeder";
    public static final String PIGLIN_BREEDER_ID = "piglin_breeder";

    public static final DeferredBlock<VillagerBreederBlock> VILLAGER_BREEDER_BLOCK =
            Registration.BLOCKS.register(VILLAGER_BREEDER_ID, () ->
                    new VillagerBreederBlock(BlockBehaviour.Properties.of().setId(
                            ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, VILLAGER_BREEDER_ID))
                    ).strength(2.0F, 6.0F).noOcclusion()
                            .isRedstoneConductor((state, getter, pos) -> false)
                            .isSuffocating((state, getter, pos) -> false)
                            .isViewBlocking((state, getter, pos) -> false))
            );

    public static final DeferredBlock<PiglinBreederBlock> PIGLIN_BREEDER_BLOCK =
            Registration.BLOCKS.register(PIGLIN_BREEDER_ID, () ->
                    new PiglinBreederBlock(BlockBehaviour.Properties.of().setId(
                            ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, PIGLIN_BREEDER_ID))
                    ).strength(2.0F, 6.0F).noOcclusion()
                            .isRedstoneConductor((state, getter, pos) -> false)
                            .isSuffocating((state, getter, pos) -> false)
                            .isViewBlocking((state, getter, pos) -> false))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VillagerBreederBlockEntity>> VILLAGER_BREEDER_BLOCK_ENTITY =
            Registration.BLOCK_ENTITY_TYPES.register(VILLAGER_BREEDER_ID, () ->
                    new BlockEntityType<>(VillagerBreederBlockEntity::new, VILLAGER_BREEDER_BLOCK.get())
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PiglinBreederBlockEntity>> PIGLIN_BREEDER_BLOCK_ENTITY =
            Registration.BLOCK_ENTITY_TYPES.register(PIGLIN_BREEDER_ID, () ->
                    new BlockEntityType<>(PiglinBreederBlockEntity::new, PIGLIN_BREEDER_BLOCK.get())
            );

    public static final DeferredItem<BlockItem> VILLAGER_BREEDER_ITEM =
            Registration.ITEMS.register(VILLAGER_BREEDER_ID, () -> new BlockItem(
                    VILLAGER_BREEDER_BLOCK.get(),
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, VILLAGER_BREEDER_ID)))
            ));

    public static final DeferredItem<BlockItem> PIGLIN_BREEDER_ITEM =
            Registration.ITEMS.register(PIGLIN_BREEDER_ID, () -> new BlockItem(
                    PIGLIN_BREEDER_BLOCK.get(),
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, PIGLIN_BREEDER_ID)))
            ));

    public static final DeferredHolder<MenuType<?>, MenuType<BreederMenu>> VILLAGER_BREEDER_MENU =
            Registration.MENU_TYPES.register(VILLAGER_BREEDER_ID, () -> new MenuType<>(BreederMenu::villager, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<BreederMenu>> PIGLIN_BREEDER_MENU =
            Registration.MENU_TYPES.register(PIGLIN_BREEDER_ID, () -> new MenuType<>(BreederMenu::piglin, FeatureFlags.VANILLA_SET));

    private BreederRegistrationAdapter() {
    }

    public static void load() {
        // Forces class loading so all DeferredRegister entries are created.
    }
}
