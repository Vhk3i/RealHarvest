package com.realharvest.event;

import com.realharvest.RealHarvest;
import com.realharvest.armor.ModArmorMaterials;
import com.realharvest.entity.CannibalEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealHarvest.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SurvivalEffectsHandler {

    @SubscribeEvent
    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (event.getEntity() instanceof Player player) {
            int leafPieces = 0;
            for (ItemStack armor : player.getArmorSlots()) {
                if (armor.getItem() instanceof ArmorItem ai && ai.getMaterial() == ModArmorMaterials.LEAF) {
                    leafPieces++;
                }
            }
            if (leafPieces > 0) {
                event.modifyVisibility(1.0 - leafPieces * 0.18);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof CannibalEntity cannibal && cannibal.isOnFire()) {
            int count = 2 + cannibal.getRandom().nextInt(3);
            event.getDrops().add(new ItemEntity(cannibal.level(),
                    cannibal.getX(), cannibal.getY() + 0.5, cannibal.getZ(),
                    new ItemStack(Items.BONE, count)));
        }
    }
}
