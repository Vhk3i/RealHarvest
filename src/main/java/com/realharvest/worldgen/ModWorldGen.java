package com.realharvest.worldgen;

import com.mojang.serialization.Codec;
import com.realharvest.RealHarvest;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModWorldGen {
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, RealHarvest.MOD_ID);

    public static final RegistryObject<Codec<ForestIslandChunkGenerator>> FOREST_ISLAND =
            CHUNK_GENERATORS.register("forest_island", () -> ForestIslandChunkGenerator.CODEC);
}
