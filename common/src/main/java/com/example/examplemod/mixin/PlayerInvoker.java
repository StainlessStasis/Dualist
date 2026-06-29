package com.example.examplemod.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Player.class)
public interface PlayerInvoker {
    @Invoker("cannotAttack")
    boolean examplemod$invokeCannotAttack(Entity entity);

    @Invoker("createAttackSource")
    DamageSource examplemod$invokeCreateAttackSource(ItemStack weapon);

    @Invoker("getEnchantedDamage")
    float examplemod$invokeGetEnchantedDamage(Entity target, float baseDamage, DamageSource damageSource);

    @Invoker("baseDamageScaleFactor")
    float examplemod$invokeBaseDamageScaleFactor();

    @Invoker("deflectProjectile")
    boolean examplemod$invokeDeflectProjectile(Entity entity);

    @Invoker("playServerSideSound")
    void examplemod$invokePlayServerSideSound(SoundEvent sound);

    @Invoker("canCriticalAttack")
    boolean examplemod$invokeCanCriticalAttack(Entity entity);

    @Invoker("isSweepAttack")
    boolean examplemod$invokeIsSweepAttack(boolean fullStrengthAttack, boolean criticalAttack, boolean knockbackAttack);

    @Invoker("doSweepAttack")
    void examplemod$invokeDoSweepAttack(Entity entity, float baseDamage, DamageSource damageSource, float attackStrengthScale);

    @Invoker("attackVisualEffects")
    void examplemod$invokeAttackVisualEffects(
            Entity entity, boolean criticalAttack, boolean sweepAttack, boolean fullStrengthAttack, boolean unused, float magicBoost);

    @Invoker("itemAttackInteraction")
    void examplemod$invokeItemAttackInteraction(Entity entity, ItemStack weapon, DamageSource damageSource, boolean wasHurt);

    @Invoker("damageStatsAndHearts")
    void examplemod$invokeDamageStatsAndHearts(Entity entity, float oldLivingEntityHealth);
}