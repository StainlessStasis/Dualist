package io.github.stainlessstasis.dualist.mixin;

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
    boolean dualist$invokeCannotAttack(Entity entity);

    @Invoker("createAttackSource")
    DamageSource dualist$invokeCreateAttackSource(ItemStack weapon);

    @Invoker("getEnchantedDamage")
    float dualist$invokeGetEnchantedDamage(Entity target, float baseDamage, DamageSource damageSource);

    @Invoker("baseDamageScaleFactor")
    float dualist$invokeBaseDamageScaleFactor();

    @Invoker("deflectProjectile")
    boolean dualist$invokeDeflectProjectile(Entity entity);

    @Invoker("playServerSideSound")
    void dualist$invokePlayServerSideSound(SoundEvent sound);

    @Invoker("canCriticalAttack")
    boolean dualist$invokeCanCriticalAttack(Entity entity);

    @Invoker("isSweepAttack")
    boolean dualist$invokeIsSweepAttack(boolean fullStrengthAttack, boolean criticalAttack, boolean knockbackAttack);

    @Invoker("attackVisualEffects")
    void dualist$invokeAttackVisualEffects(
            Entity entity, boolean criticalAttack, boolean sweepAttack, boolean fullStrengthAttack, boolean unused, float magicBoost);

    @Invoker("itemAttackInteraction")
    void dualist$invokeItemAttackInteraction(Entity entity, ItemStack weapon, DamageSource damageSource, boolean wasHurt);

    @Invoker("damageStatsAndHearts")
    void dualist$invokeDamageStatsAndHearts(Entity entity, float oldLivingEntityHealth);
}