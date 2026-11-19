package com.realharvest.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.realharvest.tree.NotchedLogBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class NotchedLogBlockEntityRenderer implements BlockEntityRenderer<NotchedLogBlockEntity> {

    public NotchedLogBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NotchedLogBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState bark = be.getOriginalState();
        BlockState innerWood = getInnerWoodState(bark);
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        byte[] notches = be.getSideNotches();

        // Глубина зарубки по каждой из 4 сторон (до 0.72 блока)
        float step = 0.24F;
        float cutW = Math.min(0.72F, notches[Direction.WEST.get2DDataValue()] * step);
        float cutE = Math.min(0.72F, notches[Direction.EAST.get2DDataValue()] * step);
        float cutN = Math.min(0.72F, notches[Direction.NORTH.get2DDataValue()] * step);
        float cutS = Math.min(0.72F, notches[Direction.SOUTH.get2DDataValue()] * step);

        // 1. Нижняя целая часть ствола (0.0 - 0.18)
        renderSlice(dispatcher, bark, poseStack, buffer, packedLight, packedOverlay,
                0.0F, 0.0F, 0.0F, 1.0F, 0.18F, 1.0F);

        // 2. Нижний скос V-образной зарубки (0.18 - 0.36) — вырез на 55% глубины
        renderCutSlice(dispatcher, innerWood, poseStack, buffer, packedLight, packedOverlay,
                0.18F, 0.18F, cutW * 0.55F, cutE * 0.55F, cutN * 0.55F, cutS * 0.55F);

        // 3. Глубокий центр 3D-зарубки (0.36 - 0.64) — вырез на 100% глубины + оголённая древесина
        renderCutSlice(dispatcher, innerWood, poseStack, buffer, packedLight, packedOverlay,
                0.36F, 0.28F, cutW, cutE, cutN, cutS);

        // 4. Верхний скос V-образной зарубки (0.64 - 0.82) — вырез на 55% глубины
        renderCutSlice(dispatcher, innerWood, poseStack, buffer, packedLight, packedOverlay,
                0.64F, 0.18F, cutW * 0.55F, cutE * 0.55F, cutN * 0.55F, cutS * 0.55F);

        // 5. Верхняя целая часть ствола (0.82 - 1.0)
        renderSlice(dispatcher, bark, poseStack, buffer, packedLight, packedOverlay,
                0.0F, 0.82F, 0.0F, 1.0F, 0.18F, 1.0F);
    }

    private void renderCutSlice(BlockRenderDispatcher dispatcher, BlockState state, PoseStack poseStack,
                                MultiBufferSource buffer, int light, int overlay,
                                float y, float height, float cutW, float cutE, float cutN, float cutS) {
        float minX = cutW;
        float maxX = 1.0F - cutE;
        if (maxX - minX < 0.16F) {
            float mid = (minX + maxX) * 0.5F;
            minX = mid - 0.08F;
            maxX = mid + 0.08F;
        }

        float minZ = cutN;
        float maxZ = 1.0F - cutS;
        if (maxZ - minZ < 0.16F) {
            float mid = (minZ + maxZ) * 0.5F;
            minZ = mid - 0.08F;
            maxZ = mid + 0.08F;
        }

        renderSlice(dispatcher, state, poseStack, buffer, light, overlay,
                minX, y, minZ, maxX - minX, height, maxZ - minZ);
    }

    private void renderSlice(BlockRenderDispatcher dispatcher, BlockState state, PoseStack poseStack,
                             MultiBufferSource buffer, int light, int overlay,
                             float x, float y, float z, float sx, float sy, float sz) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(sx, sy, sz);
        dispatcher.renderSingleBlock(state, poseStack, buffer, light, overlay);
        poseStack.popPose();
    }

    private BlockState getInnerWoodState(BlockState original) {
        Block b = original.getBlock();
        if (b == Blocks.BIRCH_LOG) return Blocks.STRIPPED_BIRCH_LOG.defaultBlockState();
        if (b == Blocks.SPRUCE_LOG) return Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
        if (b == Blocks.JUNGLE_LOG) return Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState();
        if (b == Blocks.ACACIA_LOG) return Blocks.STRIPPED_ACACIA_LOG.defaultBlockState();
        if (b == Blocks.DARK_OAK_LOG) return Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
        if (b == Blocks.MANGROVE_LOG) return Blocks.STRIPPED_MANGROVE_LOG.defaultBlockState();
        if (b == Blocks.CHERRY_LOG) return Blocks.STRIPPED_CHERRY_LOG.defaultBlockState();
        return Blocks.STRIPPED_OAK_LOG.defaultBlockState();
    }
}
