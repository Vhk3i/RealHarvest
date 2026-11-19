package com.realharvest.mining;

import com.realharvest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
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

public class ChippedBlockEntity extends BlockEntity {
    private BlockState originalState = Blocks.STONE.defaultBlockState();
    // 8 бит: 1 = октант цел, 0 = отколот. 0xFF = все 8 частей целы.
    private int voxelMask = 0xFF;

    public ChippedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHIPPED_BLOCK_BE.get(), pos, state);
    }

    public void setOriginalState(BlockState state) {
        this.originalState = state;
        setChanged();
    }

    public BlockState getOriginalState() {
        return originalState;
    }

    public int getVoxelMask() {
        return voxelMask;
    }

    /**
     * Откалывает указанный октант (или первый оставшийся, если указанный уже пуст).
     * Возвращает true, если блок полностью разрушен (voxelMask == 0).
     */
    public boolean chipOctant(int bitIndex) {
        int bit = 1 << (bitIndex & 7);
        if ((voxelMask & bit) != 0) {
            voxelMask &= ~bit;
        } else {
            int lowest = Integer.lowestOneBit(voxelMask);
            if (lowest != 0) {
                voxelMask &= ~lowest;
            }
        }

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
        return voxelMask == 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("OriginalState", NbtUtils.writeBlockState(originalState));
        tag.putInt("VoxelMask", voxelMask);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("OriginalState")) {
            originalState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("OriginalState"));
        }
        voxelMask = tag.contains("VoxelMask") ? (tag.getInt("VoxelMask") & 0xFF) : 0xFF;
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
