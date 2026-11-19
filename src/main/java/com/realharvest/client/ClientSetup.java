package com.realharvest.client;

import com.realharvest.RealHarvest;
import com.realharvest.client.render.CannibalRenderer;
import com.realharvest.client.render.ChippedBlockEntityRenderer;
import com.realharvest.client.render.FallingTreeRenderer;
import com.realharvest.client.render.NotchedLogBlockEntityRenderer;
import com.realharvest.registry.ModBlockEntities;
import com.realharvest.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RealHarvest.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FALLING_TREE.get(), FallingTreeRenderer::new);
        event.registerEntityRenderer(ModEntities.CANNIBAL.get(), CannibalRenderer::new);
        event.registerEntityRenderer(ModEntities.MOLOTOV.get(), ThrownItemRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NOTCHED_LOG_BE.get(), NotchedLogBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHIPPED_BLOCK_BE.get(), ChippedBlockEntityRenderer::new);
    }
}
