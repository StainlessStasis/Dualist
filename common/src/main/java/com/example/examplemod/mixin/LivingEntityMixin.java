package com.example.examplemod.mixin;

import com.example.examplemod.api.IOffhandEntity;
import com.example.examplemod.OffhandAttributeMath;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
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
    @Unique private float examplemod$offhandHeight;
    @Unique private float examplemod$offhandHeightOld;
    @Unique private boolean examplemod$isOffhandAttacking = false;
    @Unique private int examplemod$offhandAttackStrengthTicker = Integer.MAX_VALUE;

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    public void examplemod$swing(InteractionHand hand, boolean sendToSwingingEntity, CallbackInfo ci) {
        if (hand == InteractionHand.MAIN_HAND) return;

        LivingEntity self = (LivingEntity) (Object) this;

        boolean canSwing = !examplemod$isOffhandSwinging
                || examplemod$offhandSwingTime >= getCurrentSwingDuration() / 2
                || examplemod$offhandSwingTime < 0;

        if (canSwing) {
            examplemod$offhandSwingTime = -1;
            examplemod$isOffhandSwinging = true;

            if (self.level() instanceof ServerLevel level && canSwing) {
                ClientboundAnimatePacket packet = new ClientboundAnimatePacket(self, ClientboundAnimatePacket.SWING_OFF_HAND);
                ServerChunkCache chunkSource = level.getChunkSource();
                if (sendToSwingingEntity) {
                    chunkSource.sendToTrackingPlayersAndSelf(self, packet);
                } else {
                    chunkSource.sendToTrackingPlayers(self, packet);
                }
            }
        }

        ci.cancel();
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void examplemod$baseTick(CallbackInfo ci) {
        examplemod$offhandAttackAnimOld = examplemod$offhandAttackAnim;
        examplemod$offhandHeightOld = examplemod$offhandHeight;

        float scale = examplemod$getOffhandAttackStrengthScale(0f);
        float targetHeight = scale * scale * scale;
        examplemod$offhandHeight += Mth.clamp(targetHeight - examplemod$offhandHeight, -0.4f, 0.4f);
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

            examplemod$offhandAttackAnim = (float) examplemod$offhandSwingTime / (float) duration;

            if (examplemod$offhandSwingTime >= duration) {
                examplemod$offhandSwingTime = 0;
                examplemod$isOffhandSwinging = false;
            }
        } else {
            examplemod$offhandSwingTime = 0;
            examplemod$offhandAttackAnim = 0;
        }
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

    public void examplemod$setAttackStrengthTicker(int attackStrengthTicker) {
        this.attackStrengthTicker = attackStrengthTicker;
    }

    @Override
    public void examplemod$resetOffhandAttackStrengthTicker() {
        examplemod$offhandAttackStrengthTicker = 0;
        if (((LivingEntity)(Object)this) instanceof Player player) {
            player.resetOnlyAttackStrengthTicker();
        }
    }

    @Override
    public float examplemod$getOffhandAttackStrengthScale(float adjustTicks) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof Player player)) return 1f;

        ItemStack offhand = getItemInHand(InteractionHand.OFF_HAND);
        double attackSpeed = OffhandAttributeMath.resolveAttributes(player, Attributes.ATTACK_SPEED, offhand);
        float attackDelay = (float)((double)1.0F / attackSpeed * (double)20.0F);
        float scale = Mth.clamp((examplemod$offhandAttackStrengthTicker + adjustTicks) / attackDelay, 0.0F, 1.0F);
        return scale;
    }

    @Override
    public float examplemod$getOffhandHeight(float partialTick) {
        return Mth.lerp(partialTick, examplemod$offhandHeightOld, examplemod$offhandHeight);
    }
}