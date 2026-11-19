package com.realharvest.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.realharvest.RealHarvest;
import com.realharvest.client.model.CannibalModel;
import com.realharvest.entity.CannibalEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CannibalRenderer extends HumanoidMobRenderer<CannibalEntity, CannibalModel> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation(RealHarvest.MOD_ID, "textures/entity/cannibal_tribal.png"),
            new ResourceLocation(RealHarvest.MOD_ID, "textures/entity/cannibal_skinny.png"),
            new ResourceLocation(RealHarvest.MOD_ID, "textures/entity/cannibal_leader.png"),
            new ResourceLocation(RealHarvest.MOD_ID, "textures/entity/cannibal_pale.png")
    };

    public CannibalRenderer(EntityRendererProvider.Context context) {
        super(context, new CannibalModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    protected void scale(CannibalEntity entity, PoseStack poseStack, float partialTickTime) {
        switch (entity.getVariant()) {
            case CannibalEntity.VARIANT_SKINNY -> poseStack.scale(0.78F, 0.94F, 0.78F);
            case CannibalEntity.VARIANT_LEADER -> poseStack.scale(1.08F, 1.12F, 1.08F);
            case CannibalEntity.VARIANT_PALE -> poseStack.scale(0.92F, 0.96F, 0.92F);
            default -> poseStack.scale(0.96F, 1.0F, 0.96F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(CannibalEntity entity) {
        int idx = Math.floorMod(entity.getVariant(), TEXTURES.length);
        return TEXTURES[idx];
    }
}
