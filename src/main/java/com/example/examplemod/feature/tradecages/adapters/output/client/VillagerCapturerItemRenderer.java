package com.example.examplemod.feature.tradecages.adapters.output.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class VillagerCapturerItemRenderer extends BlockEntityWithoutLevelRenderer {
    public VillagerCapturerItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        VillagerCapturerItemRenderSupport.renderVillager(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay, 0.0F);
    }
}
