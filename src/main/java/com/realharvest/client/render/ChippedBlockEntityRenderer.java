package com.realharvest.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.realharvest.mining.ChippedBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class ChippedBlockEntityRenderer implements BlockEntityRenderer<ChippedBlockEntity> {

    public ChippedBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ChippedBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int mask = be.getVoxelMask();
        if (mask == 0) return;

        BlockState original = be.getOriginalState();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        for (int bit = 0; bit < 8; bit++) {
            if ((mask & (1 << bit)) != 0) {
                float x = (bit & 1) * 0.5F;
                float y = ((bit >> 1) & 1) * 0.5F;
                float z = ((bit >> 2) & 1) * 0.5F;

                poseStack.pushPose();
                poseStack.translate(x, y, z);
                poseStack.scale(0.5F, 0.5F, 0.5F);
                dispatcher.renderSingleBlock(original, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
        }
    }
}
