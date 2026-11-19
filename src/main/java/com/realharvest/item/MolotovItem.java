package com.realharvest.item;

import com.realharvest.entity.MolotovEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MolotovItem extends Item {
    public MolotovItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.8F, 0.7F);

        if (!level.isClientSide) {
            MolotovEntity molotov = new MolotovEntity(level, player);
            molotov.setItem(stack);
            molotov.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.35F, 1.0F);
            level.addFreshEntity(molotov);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
