package com.realharvest.tree;

import com.realharvest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class NotchedLogBlockEntity extends BlockEntity {
    public static final int MAX_CHOPS = 5;

    private BlockState originalState = Blocks.OAK_LOG.defaultBlockState();
    private final byte[] sideNotches = new byte[4]; // NORTH, SOUTH, WEST, EAST
    private int totalChops = 0;
    private Direction lastHitSide = Direction.NORTH;

    public NotchedLogBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NOTCHED_LOG_BE.get(), pos, state);
    }

    public void setOriginalState(BlockState state) {
        this.originalState = state;
        setChanged();
    }

    public BlockState getOriginalState() {
        return originalState;
    }

    public boolean addChop(Direction side, int power) {
        Direction horizontal = side.getAxis().isHorizontal() ? side : Direction.NORTH;
        this.lastHitSide = horizontal;
        int idx = horizontal.get2DDataValue();
        sideNotches[idx] = (byte) Math.min(MAX_CHOPS, sideNotches[idx] + power);
        totalChops += power;

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
        return totalChops >= MAX_CHOPS;
    }

    public int getTotalChops() {
        return totalChops;
    }

    public Direction getLastHitSide() {
        return lastHitSide;
    }

    public byte[] getSideNotches() {
        return sideNotches;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("OriginalState", NbtUtils.writeBlockState(originalState));
        tag.putByteArray("SideNotches", sideNotches);
        tag.putInt("TotalChops", totalChops);
        tag.putInt("LastHitSide", lastHitSide.get3DDataValue());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("OriginalState")) {
            originalState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("OriginalState"));
        }
        if (tag.contains("SideNotches")) {
            byte[] loaded = tag.getByteArray("SideNotches");
            System.arraycopy(loaded, 0, sideNotches, 0, Math.min(loaded.length, 4));
        }
        totalChops = tag.getInt("TotalChops");
        lastHitSide = Direction.from3DDataValue(tag.getInt("LastHitSide"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
