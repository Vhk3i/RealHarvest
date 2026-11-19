package com.realharvest.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.realharvest.RealHarvest;
import com.realharvest.client.health.ClientBodyHealth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealHarvest.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlayerAnimationHandler {

    private static boolean renderingFirstPersonBody = false;

    // 1. Рендер собственного тела от 1-го лица (видно торс и ноги, когда смотришь вниз)
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.options.getCameraType().isFirstPerson()) return;
        if (mc.player.isSpectator() || mc.player.isInvisible()) return;

        AbstractClientPlayer player = mc.player;
        EntityRenderer<? super AbstractClientPlayer> rawRenderer = mc.getEntityRenderDispatcher().getRenderer(player);
        if (!(rawRenderer instanceof PlayerRenderer playerRenderer)) return;

        float pt = event.getPartialTick();
        Vec3 cam = event.getCamera().getPosition();

        double px = Mth.lerp(pt, player.xo, player.getX());
        double py = Mth.lerp(pt, player.yo, player.getY());
        double pz = Mth.lerp(pt, player.zo, player.getZ());

        // Смещаем тело чуть назад от глаз, чтобы при взгляде вниз видеть грудь и ноги
        float bodyYaw = Mth.lerp(pt, player.yBodyRotO, player.yBodyRot);
        double rad = Math.toRadians(bodyYaw);
        double backX = Math.sin(rad) * 0.28;
        double backZ = -Math.cos(rad) * 0.28;

        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        int light = mc.getEntityRenderDispatcher().getPackedLightCoords(player, pt);

        pose.pushPose();
        pose.translate(px - cam.x + backX, py - cam.y, pz - cam.z + backZ);

        renderingFirstPersonBody = true;
        try {
            playerRenderer.render(player, bodyYaw, pt, pose, buffer, light);
        } finally {
            renderingFirstPersonBody = false;
        }

        buffer.endBatch();
        pose.popPose();
    }

    // 2. Пластика тела/торса + скрытие головы и дублирующихся рук в 1-м лице
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        PoseStack pose = event.getPoseStack();
        float pt = event.getPartialTick();
        float time = player.tickCount + pt;
        double speed = player.getDeltaMovement().horizontalDistance();

        if (renderingFirstPersonBody && player == Minecraft.getInstance().player) {
            PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
            model.head.visible = false;
            model.hat.visible = false;
            model.leftArm.visible = false;
            model.rightArm.visible = false;
            model.leftSleeve.visible = false;
            model.rightSleeve.visible = false;
        }

        // Дыхание груди/тела
        float breath = Mth.sin(time * 0.08F) * 0.015F;
        pose.scale(1.0F + breath * 0.5F, 1.0F + breath, 1.0F + breath * 0.5F);

        // Наклон торса вперёд при спринте
        if (player.isSprinting() && speed > 0.08) {
            pose.mulPose(Axis.XP.rotationDegrees(10.0F));
        }

        // Лёгкий крен при поворотах
        float yawDiff = Mth.clamp(player.yBodyRot - player.yBodyRotO, -15.0F, 15.0F);
        if (speed > 0.05) {
            pose.mulPose(Axis.ZP.rotationDegrees(-yawDiff * 0.7F));
        }

        // Хромота при травме ног
        if (player == Minecraft.getInstance().player && ClientBodyHealth.hasLegInjury() && speed > 0.03) {
            float limp = Mth.sin(time * 0.45F);
            pose.mulPose(Axis.ZP.rotationDegrees(limp * 7.5F));
            pose.translate(0.0, Math.abs(limp) * -0.06, 0.0);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (renderingFirstPersonBody && event.getEntity() == Minecraft.getInstance().player) {
            PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
            model.head.visible = true;
            model.hat.visible = true;
            model.leftArm.visible = true;
            model.rightArm.visible = true;
            model.leftSleeve.visible = true;
            model.rightSleeve.visible = true;
        }
    }

    // 3. Реалистичные анимации рук от 1-го лица (дыхание, бег, стрейф, дрожь от травм)
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float time = mc.player.tickCount + event.getPartialTick();
        double speed = mc.player.getDeltaMovement().horizontalDistance();
        PoseStack pose = event.getPoseStack();

        // Плавное дыхание рук
        float breathY = Mth.sin(time * 0.09F) * 0.015F;
        pose.translate(0.0F, breathY, 0.0F);

        // Покачивание и наклон рук при ходьбе и спринте
        if (speed > 0.02 && mc.player.onGround()) {
            float freq = mc.player.isSprinting() ? 0.65F : 0.42F;
            float amp = mc.player.isSprinting() ? 0.045F : 0.022F;
            float bobX = Mth.cos(time * freq) * amp;
            float bobY = Math.abs(Mth.sin(time * freq)) * -amp;
            pose.translate(bobX, bobY, 0.0F);
            pose.mulPose(Axis.ZP.rotationDegrees(bobX * 80.0F));
            if (mc.player.isSprinting()) {
                pose.mulPose(Axis.XP.rotationDegrees(-8.0F));
            }
        }

        // Дрожь рук при травме
        if (ClientBodyHealth.hasArmInjury()) {
            float tremorX = Mth.sin(time * 1.3F) * 0.01F;
            float tremorY = Mth.cos(time * 1.7F) * 0.01F;
            pose.translate(tremorX, tremorY, 0.0F);
        }
    }

    // 4. Дыхание головы, крен при беге и хромота камеры
    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float time = mc.player.tickCount + (float) event.getPartialTick();
        double speed = mc.player.getDeltaMovement().horizontalDistance();

        // Лёгкое дыхание камеры
        event.setPitch(event.getPitch() + Mth.sin(time * 0.08F) * 0.35F);

        // Крен камеры при ходьбе/беге
        if (speed > 0.05 && mc.player.onGround()) {
            float freq = mc.player.isSprinting() ? 0.65F : 0.42F;
            float rollAmp = mc.player.isSprinting() ? 1.4F : 0.5F;
            event.setRoll(event.getRoll() + Mth.cos(time * freq) * rollAmp);
        }

        // Хромота камеры при травме ног
        if (ClientBodyHealth.hasLegInjury() && speed > 0.03 && mc.player.onGround()) {
            event.setRoll(event.getRoll() + Mth.sin(time * 0.45F) * 3.8F);
        }
    }
}
