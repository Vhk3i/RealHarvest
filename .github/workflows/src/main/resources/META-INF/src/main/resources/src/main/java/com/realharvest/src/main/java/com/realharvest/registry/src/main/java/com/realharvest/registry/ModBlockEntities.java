package com.realharvest.registry;

import com.realharvest.RealHarvest;
import com.realharvest.mining.ChippedBlockEntity;
import com.realharvest.tree.NotchedLogBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RealHarvest.MOD_ID);

    public static final RegistryObject<BlockEntityType<NotchedLogBlockEntity>> NOTCHED_LOG_BE =
            BLOCK_ENTITIES.register("notched_log", () ->
                    BlockEntityType.Builder.of(NotchedLogBlockEntity::new, ModBlocks.NOTCHED_LOG.get()).build(null));

    public static final RegistryObject<BlockEntityType<ChippedBlockEntity>> CHIPPED_BLOCK_BE =
            BLOCK_ENTITIES.register("chipped_block", () ->
                    BlockEntityType.Builder.of(ChippedBlockEntity::new, ModBlocks.CHIPPED_BLOCK.get()).build(null));
}
