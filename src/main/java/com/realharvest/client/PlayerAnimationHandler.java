package com.realharvest.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.realharvest.RealHarvest;
import com.realharvest.client.health.ClientBodyHealth;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealHarvest.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlayerAnimationHandler {

    // Реалистичная пластика тела и торса от 3-го лица
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        PoseStack pose = event.getPoseStack();
        float pt = event.getPartialTick();
        float time = player.tickCount + pt;
        double speed = player.getDeltaMovement().horizontalDistance();

        // 1. Плавное дыхание груди/тела в покое
        float breath = Mth.sin(time * 0.08F) * 0.015F;
        pose.scale(1.0F + breath * 0.5F, 1.0F + breath, 1.0F + breath * 0.5F);

        // 2. Наклон торса вперёд при спринте и лёгкий крен при поворотах
        if (player.isSprinting() && speed > 0.08) {
            pose.mulPose(Axis.XP.rotationDegrees(10.0F));
        }

        float yawDiff = Mth.clamp(player.yBodyRot - player.yBodyRotO, -15.0F, 15.0F);
        if (speed > 0.05) {
            pose.mulPose(Axis.ZP.rotationDegrees(-yawDiff * 0.7F));
        }

        // 3. Хромота при травме ног
        if (player == Minecraft.getInstance().player && ClientBodyHealth.hasLegInjury() && speed > 0.03) {
            float limp = Mth.sin(time * 0.45F);
            pose.mulPose(Axis.ZP.rotationDegrees(limp * 7.5F));
            pose.translate(0.0, Math.abs(limp) * -0.06, 0.0);
        }
    }

    // Дыхание и дрожь рук от 1-го лица
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float time = mc.player.tickCount + event.getPartialTick();
        PoseStack pose = event.getPoseStack();

        // Плавное дыхание рук
        float breathY = Mth.sin(time * 0.09F) * 0.012F;
        pose.translate(0.0F, breathY, 0.0F);

        // Дрожь при травме рук
        if (ClientBodyHealth.hasArmInjury()) {
            float tremorX = Mth.sin(time * 1.3F) * 0.008F;
            float tremorY = Mth.cos(time * 1.7F) * 0.008F;
            pose.translate(tremorX, tremorY, 0.0F);
        }
    }

    // Дыхание головы и хромота камеры
    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float time = mc.player.tickCount + (float) event.getPartialTick();
        double speed = mc.player.getDeltaMovement().horizontalDistance();

        // Лёгкое дыхание головы
        event.setPitch(event.getPitch() + Mth.sin(time * 0.08F) * 0.35F);

        // Хромота камеры при травме ног
        if (ClientBodyHealth.hasLegInjury() && speed > 0.03 && mc.player.onGround()) {
            event.setRoll(event.getRoll() + Mth.sin(time * 0.45F) * 3.8F);
        }
    }
}
