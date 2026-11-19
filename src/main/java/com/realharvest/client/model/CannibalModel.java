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

        // Сгорбленная хищная поза + нервный наклон головы
        this.body.xRot = 0.22F;
        this.head.zRot = Mth.sin(ageInTicks * 0.12F) * 0.18F;
        this.hat.zRot = this.head.zRot;

        if (entity.isClimbing()) {
            // Быстрое карабканье по стволу дерева
            float climb = Mth.sin(ageInTicks * 0.65F);
            this.rightArm.xRot = -1.7F + climb * 0.65F;
            this.leftArm.xRot = -1.7F - climb * 0.65F;
            this.rightLeg.xRot = -0.4F - climb * 0.65F;
            this.leftLeg.xRot = -0.4F + climb * 0.65F;
        } else if (entity.isAggressive()) {
            // Дикий бег с вытянутыми вперёд руками и раскачкой
            float swing = Mth.sin(limbSwing * 0.85F) * limbSwingAmount;
            this.rightArm.xRot = -1.45F + swing * 0.7F;
            this.leftArm.xRot = -1.45F - swing * 0.7F;
            this.rightArm.zRot = 0.15F;
            this.leftArm.zRot = -0.15F;
        }
    }
}
