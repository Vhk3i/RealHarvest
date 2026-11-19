package com.realharvest.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.realharvest.RealHarvest;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealHarvest.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FirstPersonChopAnimator {
    private static float impactRecoil = 0.0F;
    private static float prevImpactRecoil = 0.0F;
    private static int chopCombo = 0;

    public static void triggerImpact() {
        impactRecoil = 1.0F;
        chopCombo = (chopCombo + 1) % 2;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        prevImpactRecoil = impactRecoil;
        if (impactRecoil > 0.0F) {
            impactRecoil = Math.max(0.0F, impactRecoil - 0.16F);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Item item = event.getItemStack().getItem();
        boolean isAxe = item instanceof AxeItem;
        boolean isPickaxe = item instanceof PickaxeItem;
        if (!isAxe && !isPickaxe) return;

        float swing = event.getSwingProgress();
        float recoil = Mth.lerp(event.getPartialTick(), prevImpactRecoil, impactRecoil);
        if (swing <= 0.001F && recoil <= 0.001F) return;

        PoseStack pose = event.getPoseStack();

        if (isAxe) {
            // Анимация The Forest: глубокий боковой замах -> резкий горизонтальный удар -> отдача
            if (swing < 0.35F) {
                // Фаза 1: Замах (отводим топор вправо и назад, разворачиваем лезвие горизонтально)
                float t = swing / 0.35F;
                float ease = Mth.sin(t * Mth.HALF_PI);
                pose.translate(0.28F * ease, 0.12F * ease, 0.22F * ease);
                pose.mulPose(Axis.YP.rotationDegrees(-45.0F * ease));
                pose.mulPose(Axis.ZP.rotationDegrees(-65.0F * ease));
                pose.mulPose(Axis.XP.rotationDegrees(25.0F * ease));
            } else {
                // Фаза 2: Мощный рубящий удар по диагонали/горизонтали влево
                float t = (swing - 0.35F) / 0.65F;
                float strike = Mth.sin(t * Mth.PI);
                pose.translate(0.28F - 0.72F * strike, 0.12F - 0.28F * strike, 0.22F - 0.45F * strike);
                pose.mulPose(Axis.YP.rotationDegrees(-45.0F + 115.0F * strike));
                pose.mulPose(Axis.ZP.rotationDegrees(-65.0F + 20.0F * strike));
                pose.mulPose(Axis.XP.rotationDegrees(25.0F - 65.0F * strike));
            }
        } else {
            // Тяжёлый удар киркой сверху вниз
            float strike = Mth.sin(swing * Mth.PI);
            pose.translate(0.0F, 0.18F * strike, -0.25F * strike);
            pose.mulPose(Axis.XP.rotationDegrees(-55.0F * strike));
        }

        // Жёсткая отдача при попадании по блоку
        if (recoil > 0.001F) {
            float kick = recoil * recoil;
            pose.translate(0.06F * kick, -0.05F * kick, 0.14F * kick);
            pose.mulPose(Axis.XP.rotationDegrees(14.0F * kick));
            pose.mulPose(Axis.ZP.rotationDegrees((chopCombo == 0 ? 10.0F : -10.0F) * kick));
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float recoil = Mth.lerp((float) event.getPartialTick(), prevImpactRecoil, impactRecoil);
        if (recoil <= 0.001F) return;

        float shake = Mth.sin(recoil * Mth.PI) * recoil;
        event.setPitch(event.getPitch() + 1.8F * shake);
        event.setRoll(event.getRoll() + (chopCombo == 0 ? 2.2F : -2.2F) * shake);
    }
}
