package com.example.examplemod.mixin;

import com.example.examplemod.api.IOffhandRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    @Final @Shadow public ModelPart head;
    @Shadow public abstract ModelPart getArm(HumanoidArm arm);

    @Inject(
            method = "setupAnim*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/HumanoidModel;setupAttackAnimation(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void examplemod$applyOffhandAttackAnim(HumanoidRenderState state, CallbackInfo ci) {
        if (!(state instanceof IOffhandRenderState offhandState)) return;
        if (!offhandState.examplemod$isOffhandSwinging()) return;

        float attackTime = offhandState.examplemod$getOffhandAttackAnim();
        if (attackTime <= 0) return;

        HumanoidArm offhandArm = state.mainArm == HumanoidArm.RIGHT
                ? HumanoidArm.LEFT
                : HumanoidArm.RIGHT;

        //vVanilla's setup already ran this frame
        if (state.attackArm == offhandArm && state.attackTime > 0) {
            return;
        }

        ModelPart arm = getArm(offhandArm);

        float bodyYRot = Mth.sin(Mth.sqrt(attackTime) * (float)(Math.PI * 2)) * 0.2F;
        if (offhandArm == HumanoidArm.LEFT) bodyYRot *= -1.0F;

        float swing = Ease.outQuart(attackTime);
        float aa = Mth.sin(swing * (float) Math.PI);
        float bb = Mth.sin(attackTime * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;

        arm.xRot -= aa * 1.2F + bb;
        arm.yRot += bodyYRot * 2.0F;
        arm.zRot += Mth.sin(attackTime * (float) Math.PI) * -0.4F;
    }
}