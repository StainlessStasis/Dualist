package io.github.stainlessstasis.dualist.mixin;

import io.github.stainlessstasis.dualist.api.IOffhandEntity;
import io.github.stainlessstasis.dualist.OffhandAttributeMath;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Weapon;
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
    @Unique private boolean dualist$isOffhandSwinging = false;
    @Unique private int dualist$offhandSwingTime = 0;
    @Unique private float dualist$offhandAttackAnim = 0;
    @Unique private float dualist$offhandAttackAnimOld = 0;
    @Unique private float dualist$offhandHeight;
    @Unique private float dualist$offhandHeightOld;
    @Unique private boolean dualist$isOffhandAttacking = false;
    @Unique private int dualist$offhandAttackStrengthTicker = Integer.MAX_VALUE;

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    public void dualist$swing(InteractionHand hand, boolean sendToSwingingEntity, CallbackInfo ci) {
        if (hand == InteractionHand.MAIN_HAND) return;

        LivingEntity self = (LivingEntity) (Object) this;

        boolean canSwing = !dualist$isOffhandSwinging
                || dualist$offhandSwingTime >= getCurrentSwingDuration() / 2
                || dualist$offhandSwingTime < 0;

        if (canSwing) {
            dualist$offhandSwingTime = -1;
            dualist$isOffhandSwinging = true;

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
    public void dualist$baseTick(CallbackInfo ci) {
        dualist$offhandAttackAnimOld = dualist$offhandAttackAnim;
        dualist$offhandHeightOld = dualist$offhandHeight;

        float scale = dualist$getOffhandAttackStrengthScale(0f);
        float targetHeight = scale * scale * scale;
        dualist$offhandHeight += Mth.clamp(targetHeight - dualist$offhandHeight, -0.4f, 0.4f);
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    public void dualist$tickOffhandStrength(CallbackInfo ci) {
        if (dualist$offhandAttackStrengthTicker < Integer.MAX_VALUE) {
            dualist$offhandAttackStrengthTicker++;
        }
    }

    @Inject(method = "updateSwingTime", at = @At("TAIL"))
    protected void dualist$updateSwingTime(CallbackInfo ci) {
        int duration = getCurrentSwingDuration();

        if (dualist$isOffhandSwinging) {
            dualist$offhandSwingTime++;

            dualist$offhandAttackAnim = (float) dualist$offhandSwingTime / (float) duration;

            if (dualist$offhandSwingTime >= duration) {
                dualist$offhandSwingTime = 0;
                dualist$isOffhandSwinging = false;
            }
        } else {
            dualist$offhandSwingTime = 0;
            dualist$offhandAttackAnim = 0;
        }
    }

    @Inject(method = "getWeaponItem", at = @At("RETURN"), cancellable = true)
    private void dualist$getWeaponItem(CallbackInfoReturnable<ItemStack> cir) {
        if (dualist$isOffhandAttacking) {
            cir.setReturnValue(getItemInHand(InteractionHand.OFF_HAND));
        }
    }

    @Inject(method = "getSecondsToDisableBlocking", at = @At("HEAD"), cancellable = true)
    private void dualist$getSecondsToDisableBlocking(CallbackInfoReturnable<Float> cir) {
        if (!dualist$isOffhandAttacking) return;

        ItemStack weaponItem = getItemInHand(InteractionHand.OFF_HAND);
        Weapon weapon = weaponItem.get(net.minecraft.core.component.DataComponents.WEAPON);
        float result = weapon != null ? weapon.disableBlockingForSeconds() : 0;
        cir.setReturnValue(result);
    }

    @Override
    public float dualist$getOffhandAttackAnim(float partialTick) {
        float delta = dualist$offhandAttackAnim - dualist$offhandAttackAnimOld;
        if (delta < 0f) delta++;
        return dualist$offhandAttackAnimOld + delta * partialTick;
    }

    @Override
    public boolean dualist$isOffhandSwinging() {
        return dualist$isOffhandSwinging;
    }

    @Override
    public void dualist$setPerformingOffhandAttack(boolean value) {
        dualist$isOffhandAttacking = value;
    }

    @Override
    public boolean dualist$isPerformingOffhandAttack() {
        return dualist$isOffhandAttacking;
    }

    public void dualist$setAttackStrengthTicker(int attackStrengthTicker) {
        this.attackStrengthTicker = attackStrengthTicker;
    }

    @Override
    public void dualist$resetOffhandAttackStrengthTicker() {
        dualist$offhandAttackStrengthTicker = 0;
        if (((LivingEntity)(Object)this) instanceof Player player) {
            player.resetOnlyAttackStrengthTicker();
        }
    }

    @Override
    public float dualist$getOffhandAttackStrengthScale(float adjustTicks) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof Player player)) return 1f;

        ItemStack offhand = getItemInHand(InteractionHand.OFF_HAND);
        double attackSpeed = OffhandAttributeMath.resolveAttributes(player, Attributes.ATTACK_SPEED, offhand);
        float attackDelay = (float)((double)1.0F / attackSpeed * (double)20.0F);
        float scale = Mth.clamp((dualist$offhandAttackStrengthTicker + adjustTicks) / attackDelay, 0.0F, 1.0F);
        return scale;
    }

    @Override
    public float dualist$getOffhandHeight(float partialTick) {
        return Mth.lerp(partialTick, dualist$offhandHeightOld, dualist$offhandHeight);
    }
}