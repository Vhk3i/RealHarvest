package com.realharvest.client.model;

import com.realharvest.entity.CannibalEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class CannibalModel extends HumanoidModel<CannibalEntity> {

    public CannibalModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(CannibalEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        int variant = entity.getVariant();

        // Разная осанка: тощий и бледный сильно сгорблены, вожак стоит прямее
        float hunch = switch (variant) {
            case CannibalEntity.VARIANT_SKINNY -> 0.38F;
            case CannibalEntity.VARIANT_PALE -> 0.30F;
            case CannibalEntity.VARIANT_LEADER -> 0.10F;
            default -> 0.20F;
        };
        this.body.xRot = hunch;

        float twitchSpeed = (variant == CannibalEntity.VARIANT_SKINNY) ? 0.24F : 0.12F;
        this.head.zRot = Mth.sin(ageInTicks * twitchSpeed) * 0.20F;
        this.hat.zRot = this.head.zRot;

        if (entity.isClimbing()) {
            float climb = Mth.sin(ageInTicks * 0.65F);
            this.rightArm.xRot = -1.7F + climb * 0.65F;
            this.leftArm.xRot = -1.7F - climb * 0.65F;
            this.rightLeg.xRot = -0.4F - climb * 0.65F;
            this.leftLeg.xRot = -0.4F + climb * 0.65F;
        } else if (entity.isAggressive()) {
            float swing = Mth.sin(limbSwing * 0.85F) * limbSwingAmount;
            this.rightArm.xRot = -1.45F + swing * 0.7F;
            this.leftArm.xRot = -1.45F - swing * 0.7F;
            this.rightArm.zRot = 0.15F;
            this.leftArm.zRot = -0.15F;
        }
    }
}
