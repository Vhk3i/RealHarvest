package com.realharvest.entity;

import com.realharvest.registry.ModEntities;
import com.realharvest.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class MolotovEntity extends ThrowableItemProjectile {
    public MolotovEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public MolotovEntity(Level level, LivingEntity shooter) {
        super(ModEntities.MOLOTOV.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MOLOTOV.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            ServerLevel level = (ServerLevel) this.level();
            BlockPos center = this.blockPosition();

            level.playSound(null, center, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.2F, 0.9F);
            level.playSound(null, center, SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 1.3F, 0.8F);

            level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.2, getZ(), 35, 1.2, 0.5, 1.2, 0.08);
            level.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.2, getZ(), 12, 0.8, 0.4, 0.8, 0.05);

            // Поджигаем землю в радиусе 3x3
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = center.offset(dx, 0, dz);
                    if (level.getBlockState(p).isAir() && BaseFireBlock.canBePlacedAt(level, p, Direction.UP)) {
                        level.setBlock(p, BaseFireBlock.getState(level, p), Block.UPDATE_ALL);
                    }
                }
            }

            // Поджигаем и раним врагов вокруг
            AABB box = new AABB(center).inflate(2.8);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box);
            for (LivingEntity e : targets) {
                e.setSecondsOnFire(6);
                e.hurt(level.damageSources().onFire(), 6.0F);
            }

            this.discard();
        }
    }
}
