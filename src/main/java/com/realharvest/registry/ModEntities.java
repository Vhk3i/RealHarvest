package com.realharvest.registry;

import com.realharvest.RealHarvest;
import com.realharvest.entity.FallingTreeEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RealHarvest.MOD_ID);

    public static final RegistryObject<EntityType<FallingTreeEntity>> FALLING_TREE =
            ENTITIES.register("falling_tree", () -> EntityType.Builder
                    .<FallingTreeEntity>of(FallingTreeEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("falling_tree"));
}
