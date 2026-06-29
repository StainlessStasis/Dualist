package com.example.examplemod.mixin;

import com.example.examplemod.mixin_api.IOffhandSwing;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IOffhandSwing {
    @Shadow protected abstract int getCurrentSwingDuration();

    @Unique private boolean examplemod$isOffhandSwinging = false;
    @Unique private int examplemod$offhandSwingTime = 0;
    @Unique private float examplemod$offhandAttackAnim = 0;
    @Unique private float examplemod$offhandAttackAnimOld = 0;

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    public void examplemod$swing(InteractionHand hand, boolean sendToSwingingEntity, CallbackInfo ci) {
        if (hand == InteractionHand.MAIN_HAND) return;

        LivingEntity entity = (LivingEntity)(Object)this;

        // trigger offhand swing if not already mid swing
        if (!examplemod$isOffhandSwinging
                || examplemod$offhandSwingTime >= getCurrentSwingDuration() / 2
                || examplemod$offhandSwingTime < 0) {

            examplemod$offhandSwingTime = -1;
            examplemod$isOffhandSwinging = true;

            if (entity.level() instanceof ServerLevel serverLevel) {
                ClientboundAnimatePacket packet = new ClientboundAnimatePacket(entity, ClientboundAnimatePacket.SWING_OFF_HAND);
                ServerChunkCache chunkSource = serverLevel.getChunkSource();
                if (sendToSwingingEntity) {
                    chunkSource.sendToTrackingPlayersAndSelf(entity, packet);
                } else {
                    chunkSource.sendToTrackingPlayers(entity, packet);
                }
            }
        }

        ci.cancel();
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void examplemod$baseTick(CallbackInfo ci) {
        examplemod$offhandAttackAnimOld = examplemod$offhandAttackAnim;
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

    @Override
    public float examplemod$getOffhandAttackAnim(float partialTick) {
        float delta = examplemod$offhandAttackAnim - examplemod$offhandAttackAnimOld;
        if (delta < 0.0F) delta++;
        return examplemod$offhandAttackAnimOld + delta * partialTick;
    }
}
