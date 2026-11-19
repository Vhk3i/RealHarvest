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

        // Проход 1: отрисовка самих 3D-блоков проекции
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

        // Проход 2: отрисовка голубых голографических рамок
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        for (BlockPos rel : rotated.keySet()) {
            BlockPos p = origin.offset(rel);
            LevelRenderer.renderLineBox(poseStack, lines,
                    p.getX(), p.getY(), p.getZ(),
                    p.getX() +
mkdir -p src/main/java/com/realharvest/block \
         src/main/resources/assets/realharvest/blockstates \
         src/main/resources/assets/realharvest/models/block

# 1. Зажигалка (LighterItem) — поджигает землю, костры и чучела
cat << 'EOF' > src/main/java/com/realharvest/item/LighterItem.java
package com.realharvest.item;

import com.realharvest.block.EffigyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class LighterItem extends Item {
    public LighterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        // Поджиг чучела
        if (state.getBlock() instanceof EffigyBlock && !state.getValue(EffigyBlock.LIT)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(EffigyBlock.LIT, true), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Поджиг обычного блока
        BlockPos firePos = pos.relative(context.getClickedFace());
        if (BaseFireBlock.canBePlacedAt(level, firePos, context.getHorizontalDirection())) {
            if (!level.isClientSide) {
                level.setBlock(firePos, BaseFireBlock.getState(level, firePos), Block.UPDATE_ALL);
                level.playSound(null, firePos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
