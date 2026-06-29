package com.example.examplemod.api;

import net.minecraft.world.item.SwingAnimationType;

public interface IOffhandRenderState {
    float examplemod$getOffhandAttackAnim();
    void examplemod$setOffhandAttackAnim(float value);
    boolean examplemod$isOffhandSwinging();
    void examplemod$setOffhandSwinging(boolean value);
    SwingAnimationType examplemod$getOffhandSwingAnimationType();
    void examplemod$setOffhandSwingAnimationType(SwingAnimationType type);
}