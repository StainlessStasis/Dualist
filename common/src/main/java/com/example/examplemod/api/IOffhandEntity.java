package com.example.examplemod.api;

import org.spongepowered.asm.mixin.Unique;

public interface IOffhandEntity {
    float examplemod$getOffhandAttackAnim(float partialTick);
    boolean examplemod$isOffhandSwinging();

    void examplemod$setPerformingOffhandAttack(boolean value);
    boolean examplemod$isPerformingOffhandAttack();

//    int examplemod$getOffhandAttackStrengthTicker();
//    void examplemod$setOffhandAttackStrengthTicker(int value);
    void examplemod$setAttackStrengthTicker(int value);
    void examplemod$resetOffhandAttackStrengthTicker();
    float examplemod$getOffhandAttackStrengthScale(float adjustTicks);
}

