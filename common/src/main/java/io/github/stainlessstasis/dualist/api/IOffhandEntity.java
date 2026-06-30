package io.github.stainlessstasis.dualist.api;

public interface IOffhandEntity {
    float dualist$getOffhandAttackAnim(float partialTick);
    boolean dualist$isOffhandSwinging();

    void dualist$setPerformingOffhandAttack(boolean value);
    boolean dualist$isPerformingOffhandAttack();

    void dualist$setAttackStrengthTicker(int value);
    void dualist$resetOffhandAttackStrengthTicker();
    float dualist$getOffhandAttackStrengthScale(float adjustTicks);

    float dualist$getOffhandHeight(float partialTick);
}

