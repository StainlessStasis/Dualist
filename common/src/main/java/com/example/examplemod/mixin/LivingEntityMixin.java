package com.example.examplemod.mixin;

import com.example.examplemod.api.IOffhandEntity;
import com.example.examplemod.OffhandAttributeMath;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IOffhandEntity {
    @Shadow protected abstract int getCurrentSwingDuration();
    @Shadow public abstract ItemStack getItemInHand(InteractionHand hand);

    @Shadow protected int attackStrengthTicker;
    @Unique private boolean examplemod$isOffhandSwinging = false;
    @Unique private int examplemod$offhandSwingTime = 0;
    @Unique private float examplemod$offhandAttackAnim = 0;
    @Unique private float examplemod$offhandAttackAnimOld = 0;
    @Unique private boolean examplemod$isOffhandAttacking = false;
    @Unique private int examplemod$offhandAttackStrengthTicker = Integer.MAX_VALUE;

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    public void examplemod$swing(InteractionHand hand, boolean sendToSwingingEntity, CallbackInfo ci) {
        if (hand == InteractionHand.MAIN_HAND) return;

        LivingEntity self = (LivingEntity) (Object) this;

        boolean canSwing = !examplemod$isOffhandSwinging
                || examplemod$offhandSwingTime >= getCurrentSwingDuration() / 2
                || examplemod$offhandSwingTime < 0;


        if (self.level() instanceof ServerLevel level && canSwing) {
            ClientboundAnimatePacket packet = new ClientboundAnimatePacket(self, ClientboundAnimatePacket.SWING_OFF_HAND);
            ServerChunkCache chunkSource = level.getChunkSource();
            if (sendToSwingingEntity) {
                chunkSource.sendToTrackingPlayersAndSelf(self, packet);
            } else {
                chunkSource.sendToTrackingPlayers(self, packet);
            }
            ci.cancel();
            return;
        }

        if (canSwing) {
            examplemod$offhandSwingTime = -1;
            examplemod$isOffhandSwinging = true;
        }
        ci.cancel();
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void examplemod$baseTick(CallbackInfo ci) {
        examplemod$offhandAttackAnimOld = examplemod$offhandAttackAnim;
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    public void examplemod$tickOffhandStrength(CallbackInfo ci) {
        if (examplemod$offhandAttackStrengthTicker < Integer.MAX_VALUE) {
            examplemod$offhandAttackStrengthTicker++;
        }
    }

    @Inject(method = "updateSwingTime", at = @At("TAIL"))
    protected void examplemod$updateSwingTime(CallbackInfo ci) {
        int duration = getCurrentSwingDuration();

        if (examplemod$isOffhandSwinging) {
            examplemod$offhandSwingTime++;
            if (examplemod$offhandSwingTime >= duration) {
                examplemod$offhandSwingTime = 0;
                examplemod$isOffhandSwinging = false;
            }
        } else {
            examplemod$offhandSwingTime = 0;
        }

        examplemod$offhandAttackAnim = (float) examplemod$offhandSwingTime / (float) duration;
    }

    @Inject(method = "getWeaponItem", at = @At("RETURN"), cancellable = true)
    private void examplemod$getWeaponItem(CallbackInfoReturnable<ItemStack> cir) {
        if (examplemod$isOffhandAttacking) {
            cir.setReturnValue(getItemInHand(InteractionHand.OFF_HAND));
        }
    }

    @Override
    public float examplemod$getOffhandAttackAnim(float partialTick) {
        float delta = examplemod$offhandAttackAnim - examplemod$offhandAttackAnimOld;
        if (delta < 0f) delta++;
        return examplemod$offhandAttackAnimOld + delta * partialTick;
    }

    @Override
    public boolean examplemod$isOffhandSwinging() {
        return examplemod$isOffhandSwinging;
    }

    @Override
    public void examplemod$setPerformingOffhandAttack(boolean value) {
        examplemod$isOffhandAttacking = value;
    }

    @Override
    public boolean examplemod$isPerformingOffhandAttack() {
        return examplemod$isOffhandAttacking;
    }

    @Override
    public int examplemod$getOffhandAttackStrengthTicker() {
        return examplemod$offhandAttackStrengthTicker;
    }

    @Override
    public void examplemod$setOffhandAttackStrengthTicker(int value) {
        examplemod$offhandAttackStrengthTicker = value;
    }

    @Override
    public void examplemod$resetOffhandAttackStrengthTicker() {
        examplemod$offhandAttackStrengthTicker = 0;
    }

    @Override
    public float examplemod$getOffhandAttackStrengthScale(float adjustTicks) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return 1f;
        }

        ItemStack offhand = getItemInHand(InteractionHand.OFF_HAND);
        double attackSpeed = OffhandAttributeMath.resolveAttributes(player, Attributes.ATTACK_SPEED, offhand);

        float scale = ((float) examplemod$offhandAttackStrengthTicker + adjustTicks) / (float) (20 / attackSpeed);
        return Mth.clamp(scale, 0f, 1f);
    }
}