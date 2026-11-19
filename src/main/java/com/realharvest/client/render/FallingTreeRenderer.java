package com.realharvest.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.realharvest.entity.FallingTreeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class FallingTreeRenderer extends EntityRenderer<FallingTreeEntity> {

    public FallingTreeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FallingTreeEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        float angle = Mth.lerp(partialTicks, entity.getPrevFallAngle(), entity.getFallAngle());
        Direction dir = entity.getFallDirection();

        poseStack.pushPose();

        switch (dir) {
            case NORTH -> poseStack.mulPose(Axis.XN.rotationDegrees(angle));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(angle));
            case EAST -> poseStack.mulPose(Axis.ZN.rotationDegrees(angle));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
            default -> {}
        }

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        for (Map.Entry<BlockPos, BlockState> entry : entity.getBlocks().entrySet()) {
            BlockPos rel = entry.getKey();
            BlockState state = entry.getValue();

            poseStack.pushPose();
            poseStack.translate(rel.getX() - 0.5, rel.getY(), rel.getZ() - 0.5);
            dispatcher.renderSingleBlock(state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(FallingTreeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
