package com.example.examplemod.api;

public interface IOffhandEntity {
    float examplemod$getOffhandAttackAnim(float partialTick);
    boolean examplemod$isOffhandSwinging();

    void examplemod$setPerformingOffhandAttack(boolean value);
    boolean examplemod$isPerformingOffhandAttack();

    int examplemod$getOffhandAttackStrengthTicker();
    void examplemod$setOffhandAttackStrengthTicker(int value);
    void examplemod$resetOffhandAttackStrengthTicker();
    float examplemod$getOffhandAttackStrengthScale(float adjustTicks);
}

