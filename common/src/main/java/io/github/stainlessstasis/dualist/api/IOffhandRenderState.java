package io.github.stainlessstasis.dualist.api;

import net.minecraft.world.item.SwingAnimationType;

public interface IOffhandRenderState {
    float dualist$getOffhandAttackAnim();
    void dualist$setOffhandAttackAnim(float value);
    boolean dualist$isOffhandSwinging();
    void dualist$setOffhandSwinging(boolean value);
    SwingAnimationType dualist$getOffhandSwingAnimationType();
    void dualist$setOffhandSwingAnimationType(SwingAnimationType type);
}