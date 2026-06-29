package com.example.examplemod;

import com.example.examplemod.mixin.LivingEntityAccessor;
import com.example.examplemod.mixin.PlayerInvoker;
import com.example.examplemod.network.OffhandAttackPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class OffhandAttack {
    public static void perform(Player player, int entityID, boolean isMiss) {
        if (player.level().isClientSide()) return;
        if (player.isSpectator()) return;

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.isEmpty()) return;
        if (!offhand.isItemEnabled(player.level().enabledFeatures())) return;
        if (player.cannotAttackWithItem(offhand, 0)) return;

        IOffhandEntity offhandEntity = (IOffhandEntity) player;

        if (isMiss) {
            offhandEntity.examplemod$resetOffhandAttackStrengthTicker();
            return;
        }

        if (entityID == OffhandAttackPacket.NO_ENTITY) return;
        Entity target = player.level().getEntity(entityID);
        if (target == null) return;

        offhandEntity.examplemod$setPerformingOffhandAttack(true);
        try {
            attackWithOffhand(player, target, offhandEntity, offhand);
        } finally {
            offhandEntity.examplemod$setPerformingOffhandAttack(false);
        }
    }

    private static void attackWithOffhand(Player player, Entity target, IOffhandEntity offhandEntity, ItemStack offhandStack) {
        System.out.println("ATTACK");
        PlayerInvoker invoker = (PlayerInvoker) player;
        LivingEntityAccessor accessor = (LivingEntityAccessor) player;
        if (invoker.examplemod$invokeCannotAttack(target)) {
            return;
        }

        boolean autoSpin = player.isAutoSpinAttack();
        float baseDamage = autoSpin
                ? (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE)
                : (float) OffhandAttributeMath.resolveAttributes(player, Attributes.ATTACK_DAMAGE, offhandStack);

        DamageSource damageSource = invoker.examplemod$invokeCreateAttackSource(offhandStack);
        float attackStrengthScale = offhandEntity.examplemod$getOffhandAttackStrengthScale(0.5F);
        float magicBoost = attackStrengthScale * (invoker.examplemod$invokeGetEnchantedDamage(target, baseDamage, damageSource) - baseDamage);
        baseDamage *= invoker.examplemod$invokeBaseDamageScaleFactor();
        System.out.println("BASE DAMAGE: "+baseDamage);

        player.onAttack();

        if (invoker.examplemod$invokeDeflectProjectile(target)) {
            return;
        }

        if (baseDamage <= 0.0F && magicBoost <= 0.0F) {
            player.postPiercingAttack();
            return;
        }

        boolean fullStrengthAttack = attackStrengthScale > 0.9F;
        boolean knockbackAttack;
        if (player.isSprinting() && fullStrengthAttack) {
            invoker.examplemod$invokePlayServerSideSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK);
            knockbackAttack = true;
        } else {
            knockbackAttack = false;
        }

        baseDamage += offhandStack.getItem().getAttackDamageBonus(target, baseDamage, damageSource);
        boolean criticalAttack = fullStrengthAttack && invoker.examplemod$invokeCanCriticalAttack(target);
        if (criticalAttack) {
            baseDamage *= 1.5F;
        }

        float totalDamage = baseDamage + magicBoost;
        System.out.println("TOTAL DAMAGE: "+totalDamage);
        boolean sweepAttack = invoker.examplemod$invokeIsSweepAttack(fullStrengthAttack, criticalAttack, knockbackAttack);

        float oldLivingEntityHealth = 0.0F;
        if (target instanceof LivingEntity livingTarget) {
            oldLivingEntityHealth = livingTarget.getHealth();
        }

        Vec3 oldMovement = target.getDeltaMovement();
        boolean wasHurt = target.hurtOrSimulate(damageSource, totalDamage);
        if (wasHurt) {
            player.causeExtraKnockback(
                    target,
                    accessor.examplemod$invokeGetKnockback(target, damageSource) + (knockbackAttack ? 0.5F : 0.0F),
                    oldMovement,
                    damageSource,
                    totalDamage,
                    true
            );
            if (sweepAttack) {
                invoker.examplemod$invokeDoSweepAttack(target, baseDamage, damageSource, attackStrengthScale);
            }

            invoker.examplemod$invokeAttackVisualEffects(target, criticalAttack, sweepAttack, fullStrengthAttack, false, magicBoost);
            player.setLastHurtMob(target);
            invoker.examplemod$invokeItemAttackInteraction(target, offhandStack, damageSource, true);
            invoker.examplemod$invokeDamageStatsAndHearts(target, oldLivingEntityHealth);
            player.causeFoodExhaustion(0.1f);
        } else {
            invoker.examplemod$invokePlayServerSideSound(SoundEvents.PLAYER_ATTACK_NODAMAGE);
        }

        offhandEntity.examplemod$resetOffhandAttackStrengthTicker();
        player.postPiercingAttack();
    }
}