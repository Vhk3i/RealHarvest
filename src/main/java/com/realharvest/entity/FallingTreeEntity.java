package com.realharvest.entity;

import com.realharvest.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FallingTreeEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Float> FALL_ANGLE =
            SynchedEntityData.defineId(FallingTreeEntity.class, EntityDataSerializers.FLOAT);

    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private Direction fallDirection = Direction.NORTH;

    private float angularVelocity = 0.8F;
    private float prevFallAngle = 0.0F;
    private boolean playedCreak = false;

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
            if (!playedCreak) {
                level().playSound(null, blockPosition(), SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.4F, 0.55F);
                playedCreak = true;
            }

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
        int maxHeight = 1;
        for (BlockPos rel : blocks.keySet()) {
            if (rel.getY() > maxHeight) maxHeight = rel.getY();
        }

        // Звук тяжёлого падения и удара о землю
        BlockPos midPos = blockPosition().relative(fallDirection, Math.max(1, maxHeight / 2));
        level.playSound(null, midPos, SoundEvents.WOOD_FALL, SoundSource.BLOCKS, 1.8F, 0.6F);
        level.playSound(null, midPos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.4F, 0.5F);

        // Разрушение ландшафта вдоль линии падения ствола
        damageTerrainAlongPath(level, maxHeight);

        // Урон сущностям вдоль линии падения
        BlockPos endPos = blockPosition().relative(fallDirection, maxHeight + 1);
        AABB impactBox = new AABB(blockPosition(), endPos).inflate(1.2, 1.5, 1.2);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, impactBox);
        for (LivingEntity victim : victims) {
            victim.hurt(level.damageSources().fallingBlock(this), 8.0F);
        }

        // Разрушение листвы (частицы + палки) и дроп брёвен вдоль ствола
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos rel = entry.getKey();
            BlockState state = entry.getValue();
            BlockPos dropPos = blockPosition().relative(fallDirection, Math.max(1, rel.getY()));

            if (state.is(BlockTags.LOGS)) {
                Block.dropResources(state, level, dropPos);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                        dropPos.getX() + 0.5, dropPos.getY() + 0.5, dropPos.getZ() + 0.5,
                        8, 0.4, 0.3, 0.4, 0.08);
            } else if (state.is(BlockTags.LEAVES)) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                        dropPos.getX() + 0.5, dropPos.getY() + 0.6, dropPos.getZ() + 0.5,
                        6, 0.6, 0.4, 0.6, 0.05);
                if (level.random.nextFloat() < 0.15F) {
                    Block.popResource(level, dropPos, new ItemStack(Items.STICK));
                }
            }
        }
    }

    private void damageTerrainAlongPath(ServerLevel level, int length) {
        Direction sideDir = fallDirection.getClockWise();

        for (int dist = 1; dist <= length; dist++) {
            BlockPos center = blockPosition().relative(fallDirection, dist);

            for (int side = -1; side <= 1; side++) {
                BlockPos col = center.relative(sideDir, side);

                // 1. Сносим хрупкие препятствия на уровне падения и чуть выше
                for (int dy = 0; dy <= 1; dy++) {
                    BlockPos target = col.above(dy);
                    BlockState st = level.getBlockState(target);
                    if (canSmashBlock(level, target, st)) {
                        level.destroyBlock(target, true);
                    }
                }

                // 2. Повреждаем сам грунт под стволом (борозда в земле и содранный дёрн)
                BlockPos groundPos = col.below();
                BlockState ground = level.getBlockState(groundPos);
                if (isSoftGround(ground)) {
                    if (side == 0 && level.random.nextFloat() < 0.65F) {
                        level.destroyBlock(groundPos, true);
                    } else if (ground.is(Blocks.GRASS_BLOCK)) {
                        level.setBlock(groundPos, Blocks.COARSE_DIRT.defaultBlockState(), Block.UPDATE_ALL);
                    }
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                            groundPos.getX() + 0.5, groundPos.getY() + 1.0, groundPos.getZ() + 0.5,
                            6, 0.3, 0.15, 0.3, 0.08);
                }
            }
        }
    }

    private boolean canSmashBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir() || !state.getFluidState().isEmpty()) return false;
        if (state.hasBlockEntity()) return false; // Не ломаем сундуки, печки и др.
        float hardness = state.getDestroySpeed(level, pos);
        return hardness >= 0.0F && hardness <= 2.0F;
    }

    private boolean isSoftGround(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAND)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.CLAY);
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
