package com.realharvest.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ForestIslandChunkGenerator extends ChunkGenerator {
    public static final Codec<ForestIslandChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, ForestIslandChunkGenerator::new)
    );

    private static final int SEA_LEVEL = 63;
    private static final int ISLAND_RADIUS = 520;
    private static final int SINKHOLE_RADIUS = 46;

    public ForestIslandChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private int computeSurfaceHeight(int x, int z) {
        double dist = Math.sqrt(x * x + z * z);
        double angle = Math.atan2(z, x);
        double coastNoise = Math.sin(angle * 3.0) * 45.0 + Math.cos(angle * 7.0) * 20.0;
        double effectiveRadius = ISLAND_RADIUS + coastNoise;

        if (dist > effectiveRadius + 40) return 42;

        double hills = Math.sin(x * 0.015) * Math.cos(z * 0.015) * 14.0
                + Math.sin(x * 0.04 + z * 0.03) * 5.0;
        double islandFactor = Math.max(0.0, Math.min(1.0, (effectiveRadius - dist) / 90.0));
        double height = 42.0 + islandFactor * (34.0 + hills);

        if (z < -180 && islandFactor > 0.2) {
            double northFactor = Math.min(1.0, (-180.0 - z) / 220.0);
            double ridge = Math.abs(Math.sin(x * 0.012) * Math.cos(z * 0.012)) * 42.0;
            height += northFactor * islandFactor * (32.0 + ridge);
        }

        if (dist < SINKHOLE_RADIUS + 18) {
            double sinkT = Math.max(0.0, Math.min(1.0, (SINKHOLE_RADIUS + 18 - dist) / 22.0));
            double craterFloor = 14.0 + Math.sin(x * 0.15) * Math.cos(z * 0.15) * 2.0;
            height = height * (1.0 - sinkT) + craterFloor * sinkT;
        }

        return (int) Math.round(Math.max(-50, Math.min(220, height)));
    }

    private boolean isCave(int x, int y, int z, int surfaceY) {
        if (y <= -55 || y >= surfaceY - 4) return false;
        double n1 = Math.sin(x * 0.07) * Math.cos(y * 0.09) * Math.sin(z * 0.07);
        double n2 = Math.cos(x * 0.035 + z * 0.035) * Math.sin(y * 0.05);
        return (n1 + n2 * 0.6) > 0.58;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        int chunkX = chunk.getPos().getMinBlockX();
        int chunkZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = chunkX + lx;
                int wz = chunkZ + lz;
                int surfaceY = computeSurfaceHeight(wx, wz);
                double distCenter = Math.sqrt(wx * wx + wz * wz);
                boolean inSinkhole = distCenter < SINKHOLE_RADIUS;
                boolean isNorthMountain = wz < -210 && surfaceY > 98;

                for (int y = getMinY(); y <= Math.max(surfaceY, SEA_LEVEL); y++) {
                    pos.set(lx, y, lz);
                    BlockState state;

                    if (y == getMinY()) {
                        state = Blocks.BEDROCK.defaultBlockState();
                    } else if (y <= surfaceY) {
                        if (isCave(wx, y, wz, surfaceY)) {
                            state = (y < -45) ? Blocks.LAVA.defaultBlockState() : Blocks.AIR.defaultBlockState();
                        } else if (y == surfaceY) {
                            if (inSinkhole) {
                                state = (y <= 20) ? Blocks.GRAVEL.defaultBlockState() : Blocks.STONE.defaultBlockState();
                            } else if (isNorthMountain) {
                                state = (y > 110) ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.STONE.defaultBlockState();
                            } else if (surfaceY <= SEA_LEVEL + 2) {
                                state = Blocks.SAND.defaultBlockState();
                            } else {
                                state = Blocks.GRASS_BLOCK.defaultBlockState();
                            }
                        } else if (y >= surfaceY - 3 && !inSinkhole && !isNorthMountain) {
                            state = (surfaceY <= SEA_LEVEL + 2) ? Blocks.SAND.defaultBlockState() : Blocks.DIRT.defaultBlockState();
                        } else {
                            state = (y < 0) ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
                        }
                    } else {
                        if (inSinkhole) {
                            state = (y <= 21) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                        } else {
                            state = (y <= SEA_LEVEL) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                        }
                    }

                    if (!state.isAir()) {
                        chunk.setBlockState(pos, state, false);
                    }
                }

                if (wx >= -3 && wx <= 3 && wz >= 88 && wz <= 104) {
                    int base = surfaceY + 1;
                    boolean wall = (wx == -3 || wx == 3);
                    for (int dy = 0; dy <= 3; dy++) {
                        pos.set(lx, base + dy, lz);
                        if (dy == 0) {
                            chunk.setBlockState(pos, Blocks.IRON_BLOCK.defaultBlockState(), false);
                        } else if (dy == 3 && !wall) {
                            chunk.setBlockState(pos, Blocks.SMOOTH_QUARTZ.defaultBlockState(), false);
                        } else if (wall && dy < 3) {
                            BlockState s = (dy == 2 && wz % 3 == 0)
                                    ? Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState()
                                    : Blocks.SMOOTH_QUARTZ.defaultBlockState();
                            chunk.setBlockState(pos, s, false);
                        }
                    }
                    if (wx == 0 && wz == 88) {
                        chunk.setBlockState(pos.set(lx, base + 1, lz), Blocks.CAMPFIRE.defaultBlockState(), false);
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {}

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {}

    @Override public int getGenDepth() { return 384; }
    @Override public int getSeaLevel() { return SEA_LEVEL; }
    @Override public int getMinY() { return -64; }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return Math.max(computeSurfaceHeight(x, z), SEA_LEVEL);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        int h = computeSurfaceHeight(x, z);
        BlockState[] states = new BlockState[Math.max(1, h - getMinY() + 1)];
        for (int i = 0; i < states.length; i++) states[i] = Blocks.STONE.defaultBlockState();
        return new NoiseColumn(getMinY(), states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("Forest Island Generator");
    }
}
