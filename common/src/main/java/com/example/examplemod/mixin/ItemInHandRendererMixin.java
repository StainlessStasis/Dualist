package com.example.examplemod.mixin;

import com.example.examplemod.IOffhandEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Redirect(
            method = "submitHandsWithItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getAttackAnim(F)F"
            )
    )
    private float examplemod$redirectAttackAnim(LocalPlayer player, float frameInterp) {
        IOffhandEntity offhandEntity = (IOffhandEntity) player;
        if (offhandEntity.examplemod$isOffhandSwinging()) {
            return offhandEntity.examplemod$getOffhandAttackAnim(frameInterp);
        }
        return player.getAttackAnim(frameInterp);
    }

    @ModifyVariable(
            method = "submitHandsWithItems",
            at = @At(value = "LOAD", ordinal = 0),
            name = "attackHand"
    )
    private InteractionHand examplemod$modifyAttackHand(InteractionHand original,
                                                        float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                                        LocalPlayer player, int lightCoords) {
        IOffhandEntity offhandEntity = (IOffhandEntity) player;
        if (offhandEntity.examplemod$isOffhandSwinging()) return InteractionHand.OFF_HAND;
        return original;
    }
}