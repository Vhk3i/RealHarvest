package com.realharvest.entity;

import com.realharvest.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import java.util.LinkedHashMap;
import java.util.Map;

public class FallingTreeEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Float> FALL_ANGLE =
            SynchedEntityData.defineId(FallingTreeEntity.class, EntityDataSerializers.FLOAT);

    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private Direction fallDirection = Direction.NORTH;

    private float angularVelocity = 0.8F;
    private float prevFallAngle = 0.0F;

    public FallingTreeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public FallingTreeEntity(Level level, BlockPos origin, Map<BlockPos, BlockState> worldBlocks, Direction fallDir) {
        this(ModEntities.FALLING_TREE.get(), level);
        this.setPos(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
        this.fallDirection = fallDir.getAxis().isHorizontal() ? fallDir : Direction.NORTH;

        for (Map.Entry<BlockPos, BlockState> entry : worldBlocks.entrySet()) {
            BlockPos relative = entry.getKey().subtract(origin);
            this.blocks.put(relative, entry.getValue());
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(FALL_ANGLE, 0.0F);
    }

    public float getFallAngle() {
        return this.entityData.get(FALL_ANGLE);
    }

    public float getPrevFallAngle() {
        return this.prevFallAngle;
    }

    public Direction getFallDirection() {
        return this.fallDirection;
    }

    public Map<BlockPos, BlockState> getBlocks() {
        return this.blocks;
    }

    @Override
    public void tick() {
        super.tick();
        this.prevFallAngle = getFallAngle();

        if (!level().isClientSide) {
            float angle = getFallAngle();
            double rad = Math.toRadians(Math.max(angle, 5.0F));
            angularVelocity += (float) (0.35 * Math.sin(rad) + 0.08);
            angle += angularVelocity;

            if (angle >= 90.0F) {
                this.entityData.set(FALL_ANGLE, 90.0F);
                onTreeLanded((ServerLevel) level());
                this.discard();
            } else {
                this.entityData.set(FALL_ANGLE, angle);
            }
        }
    }

    private void onTreeLanded(ServerLevel level) {
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos rel = entry.getKey();
            BlockState state = entry.getValue();

            BlockPos dropPos = blockPosition().relative(fallDirection, Math.max(1, rel.getY()));
            if (state.is(BlockTags.LOGS)) {
                Block.dropResources(state, level, dropPos);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.fallDirection = Direction.from3DDataValue(tag.getInt("FallDir"));
        this.entityData.set(FALL_ANGLE, tag.getFloat("FallAngle"));
        this.angularVelocity = tag.getFloat("AngVel");

        this.blocks.clear();
        ListTag list = tag.getList("Blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            BlockPos pos = new BlockPos(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z"));
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), entry.getCompound("State"));
            this.blocks.put(pos, state);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("FallDir", this.fallDirection.get3DDataValue());
        tag.putFloat("FallAngle", getFallAngle());
        tag.putFloat("AngVel", this.angularVelocity);

        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            CompoundTag item = new CompoundTag();
            item.putInt("X", entry.getKey().getX());
            item.putInt("Y", entry.getKey().getY());
            item.putInt("Z", entry.getKey().getZ());
            item.put("State", NbtUtils.writeBlockState(entry.getValue()));
            list.add(item);
        }
        tag.put("Blocks", list);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(fallDirection.get3DDataValue());
        buffer.writeVarInt(blocks.size());
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            buffer.writeBlockPos(entry.getKey());
            buffer.writeVarInt(Block.getId(entry.getValue()));
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.fallDirection = Direction.from3DDataValue(buffer.readInt());
        int size = buffer.readVarInt();
        this.blocks.clear();
        for (int i = 0; i < size; i++) {
            BlockPos rel = buffer.readBlockPos();
            BlockState state = Block.stateById(buffer.readVarInt());
            this.blocks.put(rel, state);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
