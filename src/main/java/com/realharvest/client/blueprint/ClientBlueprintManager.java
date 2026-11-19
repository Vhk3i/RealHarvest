package com.realharvest.client.blueprint;

import com.realharvest.blueprint.Blueprint;
import com.realharvest.network.ModNetwork;
import com.realharvest.network.packet.C2SPlaceBlueprintPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ClientBlueprintManager {
    private static Blueprint activeBlueprint = null;
    private static int rotation = 0;

    public static Blueprint getActiveBlueprint() {
        return activeBlueprint;
    }

    public static int getRotation() {
        return rotation;
    }

    public static void setActiveBlueprint(Blueprint bp) {
        activeBlueprint = bp;
        rotation = 0;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && bp != null) {
            mc.player.displayClientMessage(Component.literal(
                    "Чертёж: " + bp.getTitle() + " | ПКМ — построить, Shift+ПКМ — повернуть"), true);
        }
    }

    public static void rotate() {
        rotation = (rotation + 1) % 4;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("Поворот: " + (rotation * 90) + "°"), true);
        }
    }

    public static void clear() {
        activeBlueprint = null;
    }

    public static BlockPos getTargetPos() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;
        HitResult hit = mc.player.pick(8.0D, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bhr = (BlockHitResult) hit;
            return bhr.getBlockPos().relative(bhr.getDirection());
        }
        return null;
    }

    public static void tryPlaceActive() {
        if (activeBlueprint == null) return;
        BlockPos target = getTargetPos();
        if (target != null) {
            ModNetwork.CHANNEL.sendToServer(new C2SPlaceBlueprintPacket(activeBlueprint.getId(), target, rotation));
            activeBlueprint = null;
        }
    }
}
