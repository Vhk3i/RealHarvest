package com.realharvest.registry;

import com.realharvest.RealHarvest;
import com.realharvest.mining.ChippedBlock;
import com.realharvest.tree.NotchedLogBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RealHarvest.MOD_ID);

    public static final RegistryObject<Block> NOTCHED_LOG = BLOCKS.register("notched_log",
            () -> new NotchedLogBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .dynamicShape()));

    public static final RegistryObject<Block> CHIPPED_BLOCK = BLOCKS.register("chipped_block",
            () -> new ChippedBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .dynamicShape()));
}
