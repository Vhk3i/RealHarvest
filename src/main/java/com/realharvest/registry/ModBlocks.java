package com.realharvest.registry;

import com.realharvest.RealHarvest;
import com.realharvest.block.EffigyBlock;
import com.realharvest.block.SpikeTrapBlock;
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

    public static final RegistryObject<Block> SPIKE_TRAP = BLOCKS.register("spike_trap",
            () -> new SpikeTrapBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f)
                    .noOcclusion()
                    .noCollission()));

    public static final RegistryObject<Block> EFFIGY = BLOCKS.register("effigy",
            () -> new EffigyBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(EffigyBlock.LIT) ? 14 : 0)));
}
