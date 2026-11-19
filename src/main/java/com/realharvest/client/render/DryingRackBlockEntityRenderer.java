package com.realharvest.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.realharvest.block.DryingRackBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DryingRackBlockEntityRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {

    public DryingRackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DryingRackBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stack = be.getHangingItem();
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.58, 0.5);
        poseStack.scale(0.65F, 0.65F, 0.65F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, be.getLevel(), 0);
        poseStack.popPose();
    }
}
