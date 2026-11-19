package com.realharvest.client.render;

import com.realharvest.RealHarvest;
import com.realharvest.client.model.CannibalModel;
import com.realharvest.entity.CannibalEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CannibalRenderer extends HumanoidMobRenderer<CannibalEntity, CannibalModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RealHarvest.MOD_ID, "textures/entity/cannibal.png");

    public CannibalRenderer(EntityRendererProvider.Context context) {
        super(context, new CannibalModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CannibalEntity entity) {
        return TEXTURE;
    }
}
