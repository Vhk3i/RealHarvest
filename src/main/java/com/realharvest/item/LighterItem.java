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
