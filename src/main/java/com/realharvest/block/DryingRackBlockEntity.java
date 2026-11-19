package com.realharvest.block;

import com.realharvest.registry.ModBlockEntities;
import com.realharvest.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DryingRackBlockEntity extends BlockEntity {
    public static final int DRY_TIME = 200; // 10 секунд
    private ItemStack hangingItem = ItemStack.EMPTY;
    private int dryProgress = 0;

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK_BE.get(), pos, state);
    }

    public static boolean isRawMeat(ItemStack stack) {
        return stack.is(Items.BEEF) || stack.is(Items.PORKCHOP) || stack.is(Items.MUTTON)
                || stack.is(Items.CHICKEN) || stack.is(Items.RABBIT);
    }

    public ItemStack getHangingItem() {
        return hangingItem;
    }

    public boolean placeMeat(ItemStack stack) {
        if (!hangingItem.isEmpty() || !isRawMeat(stack)) return false;
        this.hangingItem = stack.copyWithCount(1);
        this.dryProgress = 0;
        sync();
        return true;
    }

    public ItemStack takeItem() {
        if (hangingItem.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = hangingItem.copy();
        hangingItem = ItemStack.EMPTY;
        dryProgress = 0;
        sync();
        return out;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity be) {
        if (!be.hangingItem.isEmpty() && isRawMeat(be.hangingItem)) {
            be.dryProgress++;
            if (be.dryProgress >= DRY_TIME) {
                be.hangingItem = new ItemStack(ModItems.DRIED_MEAT.get());
                be.dryProgress = 0;
                level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.5F, 1.3F);
                be.sync();
            }
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!hangingItem.isEmpty()) {
            tag.put("HangingItem", hangingItem.save(new CompoundTag()));
        }
        tag.putInt("DryProgress", dryProgress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        hangingItem = tag.contains("HangingItem") ? ItemStack.of(tag.getCompound("HangingItem")) : ItemStack.EMPTY;
        dryProgress = tag.getInt("DryProgress");
    }

    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) load(pkt.getTag());
    }
}
