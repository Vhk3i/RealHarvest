package com.realharvest.client.blueprint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.realharvest.RealHarvest;
import com.realharvest.blueprint.Blueprint;
import com.realharvest.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = RealHarvest.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BlueprintProjectionRenderer {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (!mc.player.getMainHandItem().is(ModItems.SURVIVAL_GUIDE.get())
                && !mc.player.getOffhandItem().is(ModItems.SURVIVAL_GUIDE.get())) {
            return;
        }

        Blueprint bp = ClientBlueprintManager.getActiveBlueprint();
        if (bp == null) return;

        BlockPos origin = ClientBlueprintManager.getTargetPos();
        if (origin == null) return;

        Map<BlockPos, BlockState> rotated = bp.getRotatedBlocks(ClientBlueprintManager.getRotation());
        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        for (Map.Entry<BlockPos, BlockState> entry : rotated.entrySet()) {
            BlockPos p = origin.offset(entry.getKey());
            poseStack.pushPose();
            poseStack.translate(p.getX() + 0.05, p.getY() + 0.05, p.getZ() + 0.05);
            poseStack.scale(0.9F, 0.9F, 0.9F);
            dispatcher.renderSingleBlock(entry.getValue(), poseStack, buffer,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        buffer.endBatch();

        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        for (BlockPos rel : rotated.keySet()) {
            BlockPos p = origin.offset(rel);
            LevelRenderer.renderLineBox(poseStack, lines,
                    p.getX(), p.getY(), p.getZ(),
                    p.getX() + 1.0, p.getY() + 1.0, p.getZ() + 1.0,
                    0.35F, 0.78F, 1.0F, 0.85F);
        }
        buffer.endBatch(RenderType.lines());

        poseStack.popPose();
    }
}
