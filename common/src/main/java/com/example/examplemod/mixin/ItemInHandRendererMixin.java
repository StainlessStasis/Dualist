package com.example.examplemod.mixin;

import com.example.examplemod.mixin_api.IOffhandSwing;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow private float oMainHandHeight;
    @Shadow private float mainHandHeight;
    @Shadow private float oOffHandHeight;
    @Shadow private float offHandHeight;

    @WrapOperation(
            method = "submitHandsWithItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;submitArmWithItem(" +
                            "Lnet/minecraft/client/player/AbstractClientPlayer;FF" +
                            "Lnet/minecraft/world/InteractionHand;F" +
                            "Lnet/minecraft/world/item/ItemStack;F" +
                            "Lcom/mojang/blaze3d/vertex/PoseStack;" +
                            "Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"
            )
    )
    private void examplemod$wrapSubmitHandsWithItems(
            ItemInHandRenderer instance,
            AbstractClientPlayer player,
            float frameInterp,
            float xRot,
            InteractionHand hand,
            float attack,
            ItemStack handItem,
            float inverseArmHeight,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            Operation<Void> original)
    {
        if (hand == InteractionHand.MAIN_HAND) {
            float mainAttack = player.getAttackAnim(frameInterp);
            float mainInverseArmHeight = 1.0F - Mth.lerp(frameInterp, oMainHandHeight, mainHandHeight);
            original.call(instance, player, frameInterp, xRot, hand, mainAttack, handItem, mainInverseArmHeight,
                    poseStack, submitNodeCollector, lightCoords);
        } else {
            float offAttack = ((IOffhandSwing) player).examplemod$getOffhandAttackAnim(frameInterp);
            float offInverseArmHeight = 1.0F - Mth.lerp(frameInterp, oOffHandHeight, offHandHeight);
            original.call(instance, player, frameInterp, xRot, hand, offAttack, handItem, offInverseArmHeight,
                    poseStack, submitNodeCollector, lightCoords);
        }
    }
}