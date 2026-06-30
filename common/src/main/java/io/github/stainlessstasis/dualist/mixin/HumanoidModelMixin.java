package io.github.stainlessstasis.dualist.mixin;

import io.github.stainlessstasis.dualist.api.IOffhandRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    @Final @Shadow public ModelPart head;
    @Shadow public abstract ModelPart getArm(HumanoidArm arm);
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;

    @Inject(
            method = "setupAttackAnimation",
            at = @At(value = "HEAD"),
            cancellable = true)
    public void dualist$applyOffhandAttackAnim(HumanoidRenderState state, CallbackInfo ci) {
        if (!(state instanceof IOffhandRenderState offhandState)) return;

        float offAttackTime = offhandState.dualist$getOffhandAttackAnim();
        if (offAttackTime <= 0) {
            return;
        }

        float mainAttackTime = state.attackTime;
        float ageScale = state.ageScale;

        float mainRot = 0;
        if (mainAttackTime > 0) {
            mainRot = Mth.sin(Mth.sqrt(mainAttackTime) * (float) (Math.PI * 2)) * 0.2F;
            if (state.attackArm == HumanoidArm.LEFT) mainRot *= -1.0F;
        }

        float offRot = 0;
        if (offAttackTime > 0) {
            offRot = Mth.sin(Mth.sqrt(offAttackTime) * (float) (Math.PI * 2)) * 0.2F;
            if (state.attackArm.getOpposite() == HumanoidArm.LEFT) offRot *= -1.0F;
        }

        this.body.yRot = mainRot + offRot;

        this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F * ageScale;
        this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F * ageScale;
        this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F * ageScale;
        this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F * ageScale;

        this.rightArm.yRot += this.body.yRot;
        this.rightArm.xRot += this.body.yRot;
        this.leftArm.yRot += this.body.yRot;
        this.leftArm.xRot += this.body.yRot;

        if (mainAttackTime > 0) {
            switch (state.swingAnimationType) {
                case WHACK:
                    dualist$whack(state.attackArm, mainAttackTime, mainRot);
                    break;
                case STAB:
                    SpearAnimations.thirdPersonAttackHand((HumanoidModel<? super HumanoidRenderState>)(Object)this, state);
                    break;
                default: break;
            }
        }

        if (offAttackTime > 0) {
            switch (offhandState.dualist$getOffhandSwingAnimationType()) {
                case WHACK:
                    dualist$whack(state.attackArm.getOpposite(), offAttackTime, offRot);
                    break;
                case STAB:
                    SpearAnimations.thirdPersonAttackHand((HumanoidModel<? super HumanoidRenderState>)(Object)this, state);
                    break;
                default: break;
            }
        }

        ci.cancel();
    }

    @Unique
    private void dualist$whack(HumanoidArm arm, float attackTime, float isolatedBodyRot) {
        float swing = Ease.outQuart(attackTime);
        float aa = Mth.sin(swing * (float) Math.PI);
        float bb = Mth.sin(attackTime * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
        ModelPart attackArm = this.getArm(arm);
        attackArm.xRot -= aa * 1.2F + bb;
        attackArm.yRot += isolatedBodyRot * 2.0F;
        attackArm.zRot += Mth.sin(attackTime * (float) Math.PI) * (arm == HumanoidArm.LEFT ? 0.4F : -0.4F);
    }
}