package io.github.stainlessstasis.dualist.mixin;

import io.github.stainlessstasis.dualist.api.IOffhandRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.SwingAnimationType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements IOffhandRenderState {
    @Unique private float dualist$offhandAttackAnim = 0f;
    @Unique private boolean dualist$offhandSwinging = false;
    @Unique private SwingAnimationType dualist$swingAnimType = SwingAnimationType.NONE;

    @Override
    public float dualist$getOffhandAttackAnim() {
        return dualist$offhandAttackAnim;
    }

    @Override
    public void dualist$setOffhandAttackAnim(float value) {
        dualist$offhandAttackAnim = value;
    }

    @Override
    public boolean dualist$isOffhandSwinging() {
        return dualist$offhandSwinging;
    }

    @Override
    public void dualist$setOffhandSwinging(boolean value) {
        dualist$offhandSwinging = value;
    }

    @Override
    public SwingAnimationType dualist$getOffhandSwingAnimationType() {
        return dualist$swingAnimType;
    }

    @Override
    public void dualist$setOffhandSwingAnimationType(SwingAnimationType type) {
        dualist$swingAnimType = type;
    }
}