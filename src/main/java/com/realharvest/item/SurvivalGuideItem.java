package com.realharvest.item;

import com.realharvest.client.blueprint.ClientBlueprintManager;
import com.realharvest.client.gui.SurvivalGuideScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class SurvivalGuideItem extends Item {
    public SurvivalGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                if (ClientBlueprintManager.getActiveBlueprint() != null) {
                    if (player.isShiftKeyDown()) {
                        ClientBlueprintManager.rotate();
                    } else {
                        ClientBlueprintManager.tryPlaceActive();
                    }
                } else {
                    Minecraft.getInstance().setScreen(new SurvivalGuideScreen());
                }
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
