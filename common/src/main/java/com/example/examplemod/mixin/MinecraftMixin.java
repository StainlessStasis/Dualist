package com.example.examplemod.mixin;

import com.example.examplemod.api.IOffhandEntity;
import com.example.examplemod.network.OffhandAttackPacket;
import com.example.examplemod.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow public LocalPlayer player;
    @Shadow public MultiPlayerGameMode gameMode;

    @Inject(
            method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;startUseItem()V",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void examplemod$handleOffhandAttack(CallbackInfo ci) {
        if (player == null || gameMode == null || gameMode.isSpectator() || player.isHandsBusy()) {
            return;
        }

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.isEmpty()) return;

        // only intercept if offhand doesnt have a use functionality (e.g. shield)
        if (offhand.getUseAnimation() != ItemUseAnimation.NONE) return;

        // only intercept if main hand also doesnt have a use functionality
        ItemStack mainhand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!mainhand.isEmpty() && mainhand.getUseAnimation() != ItemUseAnimation.NONE) return;

        int entityID = OffhandAttackPacket.NO_ENTITY;
        boolean isMiss = false;
        Minecraft mc = (Minecraft) (Object) this;
        if (mc.hitResult != null) {
            switch (mc.hitResult.getType()) {
                case ENTITY -> entityID = ((EntityHitResult) mc.hitResult).getEntity().getId();
                case MISS -> isMiss = true;
                case BLOCK -> {
                    return;
                }
            }
        }

        player.swing(InteractionHand.OFF_HAND);
        Services.PLATFORM.sendOffhandAttackPacket(entityID, isMiss, ((LivingEntityAccessor)player).examplemod$getAttackStrengthTicker());
        ((IOffhandEntity)player).examplemod$resetOffhandAttackStrengthTicker();
        ci.cancel();
    }
}