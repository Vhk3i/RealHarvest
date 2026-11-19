package com.realharvest;

import com.mojang.logging.LogUtils;
import com.realharvest.network.ModNetwork;
import com.realharvest.registry.ModBlockEntities;
import com.realharvest.registry.ModBlocks;
import com.realharvest.registry.ModEntities;
import com.realharvest.registry.ModItems;
import com.realharvest.worldgen.ModWorldGen;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RealHarvest.MOD_ID)
public class RealHarvest {
    public static final String MOD_ID = "realharvest";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RealHarvest() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModWorldGen.CHUNK_GENERATORS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
        LOGGER.info("RealHarvest fully initialized.");
    }
}
