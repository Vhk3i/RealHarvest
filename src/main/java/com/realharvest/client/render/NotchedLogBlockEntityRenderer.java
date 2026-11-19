package com.realharvest.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.realharvest.tree.NotchedLogBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class NotchedLogBlockEntityRenderer implements BlockEntityRenderer<NotchedLogBlockEntity> {

    public NotchedLogBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NotchedLogBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState original = be.getOriginalState();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        byte[] notches = be.getSideNotches();

        float step = 0.14F;
        float minX = Math.min(0.4F, notches[Direction.WEST.get2DDataValue()] * step);
        float maxX = Math.max(0.6F, 1.0F - notches[Direction.EAST.get2DDataValue()] * step);
        float minZ = Math.min(0.4F, notches[Direction.NORTH.get2DDataValue()] * step);
        float maxZ = Math.max(0.6F, 1.0F - notches[Direction.SOUTH.get2DDataValue()] * step);

        // Нижняя целая часть ствола
        poseStack.pushPose();
        poseStack.scale(1.0F, 0.3F, 1.0F);
        dispatcher.renderSingleBlock(original, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        // Средняя подрубленная часть (с вырезом со стороны ударов)
        poseStack.pushPose();
        poseStack.translate(minX, 0.3F, minZ);
        poseStack.scale(Math.max(0.2F, maxX - minX), 0.4F, Math.max(0.2F, maxZ - minZ));
        dispatcher.renderSingleBlock(original, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        // Верхняя целая часть ствола
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.7F, 0.0F);
        poseStack.scale(1.0F, 0.3F, 1.0F);
        dispatcher.renderSingleBlock(original, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
