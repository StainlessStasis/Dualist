package com.example.examplemod;

import com.example.examplemod.api.IOffhandEntity;
import com.example.examplemod.mixin.LivingEntityAccessor;
import com.example.examplemod.mixin.PlayerInvoker;
import com.example.examplemod.network.OffhandAttackPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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

    private static void attackWithOffhand(Player player, Entity target, IOffhandEntity offhandEntity, ItemStack offhand) {
        System.out.println("ATTACK");
        PlayerInvoker invoker = (PlayerInvoker) player;
        LivingEntityAccessor accessor = (LivingEntityAccessor) player;
        if (invoker.examplemod$invokeCannotAttack(target)) {
            return;
        }

        boolean autoSpin = player.isAutoSpinAttack();
        float baseDamage = autoSpin
                ? (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE)
                : (float) OffhandAttributeMath.resolveAttributes(player, Attributes.ATTACK_DAMAGE, offhand);

        DamageSource damageSource = invoker.examplemod$invokeCreateAttackSource(offhand);
        float attackStrengthScale = offhandEntity.examplemod$getOffhandAttackStrengthScale(0.5F);
        float magicBoost = attackStrengthScale * (invoker.examplemod$invokeGetEnchantedDamage(target, baseDamage, damageSource) - baseDamage);
        baseDamage *= invoker.examplemod$invokeBaseDamageScaleFactor();
        System.out.println("BASE DAMAGE: "+baseDamage);

        if (invoker.examplemod$invokeDeflectProjectile(target)) {
            player.onAttack();
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

        baseDamage += offhand.getItem().getAttackDamageBonus(target, baseDamage, damageSource);
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
                doOffhandSweepAttack(player, target, baseDamage, damageSource, attackStrengthScale, offhand);
            }

            invoker.examplemod$invokeAttackVisualEffects(target, criticalAttack, sweepAttack, fullStrengthAttack, false, magicBoost);
            player.setLastHurtMob(target);
            invoker.examplemod$invokeItemAttackInteraction(target, offhand, damageSource, true);
            invoker.examplemod$invokeDamageStatsAndHearts(target, oldLivingEntityHealth);
            player.causeFoodExhaustion(0.1f);
        } else {
            invoker.examplemod$invokePlayServerSideSound(SoundEvents.PLAYER_ATTACK_NODAMAGE);
        }

        offhandEntity.examplemod$resetOffhandAttackStrengthTicker();
        player.postPiercingAttack();
        player.onAttack();
    }

    private static void doOffhandSweepAttack(
            Player player, Entity target, float baseDamage, DamageSource damageSource, float attackStrengthScale, ItemStack offhand
    ) {
        PlayerInvoker invoker = (PlayerInvoker) player;
        invoker.examplemod$invokePlayServerSideSound(SoundEvents.PLAYER_ATTACK_SWEEP);

        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        double sweepingRatio = OffhandAttributeMath.resolveAttributes(player, Attributes.SWEEPING_DAMAGE_RATIO, offhand);
        float sweepDamage = 1.0F + (float) sweepingRatio * baseDamage;

        for (LivingEntity nearby : player.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(1, 0.25, 1))) {
            if (nearby == player) continue;
            if (nearby == target) continue;
            if (player.isAlliedTo(nearby)) continue;
            if (nearby instanceof ArmorStand stand && stand.isMarker()) continue;
            if (player.distanceToSqr(nearby) >= 9) continue;

            float enchantedSweep = invoker.examplemod$invokeGetEnchantedDamage(
                    nearby, sweepDamage, damageSource) * attackStrengthScale;

            if (nearby.hurtServer(serverLevel, damageSource, enchantedSweep)) {
                nearby.knockback(
                        0.4F,
                        Mth.sin(player.getYRot() * (float)(Math.PI / 180.0)),
                        -Mth.cos(player.getYRot() * (float)(Math.PI / 180.0)),
                        damageSource,
                        enchantedSweep
                );
                EnchantmentHelper.doPostAttackEffects(serverLevel, nearby, damageSource);
            }
        }

        double dx = -Mth.sin(player.getYRot() * (float)(Math.PI / 180.0));
        double dz =  Mth.cos(player.getYRot() * (float)(Math.PI / 180.0));
        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                player.getX() + dx, player.getY(0.5), player.getZ() + dz,
                0, dx, 0, dz, 0);
    }

}