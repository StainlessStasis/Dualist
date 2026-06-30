package io.github.stainlessstasis.dualist.mixin;

import io.github.stainlessstasis.dualist.api.IOffhandEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Unique private float dualist$mainHandAttackOverride;
    @Unique private float dualist$offHandAttackOverride;
    @Unique private float dualist$offhandInverseArmHeightOverride;
    @Unique private boolean dualist$frameComputed = false;

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void dualist$computeOverrides(
            float partialTick, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            LocalPlayer player, int lightCoords, CallbackInfo ci
    ) {
        IOffhandEntity offhandEntity = (IOffhandEntity) player;

        dualist$mainHandAttackOverride = (player.swinging && player.swingingArm == InteractionHand.MAIN_HAND)
                ? player.getAttackAnim(partialTick) : 0.0F;

        float offHandAttack = offhandEntity.dualist$getOffhandAttackAnim(partialTick);
        dualist$offHandAttackOverride = Math.max(offHandAttack, 0.0F);
        dualist$offhandInverseArmHeightOverride = 1 - offhandEntity.dualist$getOffhandHeight(partialTick);

        dualist$frameComputed = true;
    }

    @Redirect(
            method = "renderHandsWithItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
                    ordinal = 0
            )
    )
    private void dualist$redirectMainHandRender(
            ItemInHandRenderer self, AbstractClientPlayer player, float frameInterp, float xRot,
            InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight,
            PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords
    ) {
        float finalAttack = dualist$frameComputed ? dualist$mainHandAttackOverride : attack;
        ((ItemInHandRendererInvoker) self).dualist$invokeRenderArmWithItem(
                player, frameInterp, xRot, InteractionHand.MAIN_HAND, finalAttack,
                itemStack, inverseArmHeight, poseStack, submitNodeCollector, lightCoords
        );
    }

    @Redirect(
            method = "renderHandsWithItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
                    ordinal = 1
            )
    )
    private void dualist$redirectOffHandRender(
            ItemInHandRenderer self, AbstractClientPlayer player, float frameInterp, float xRot,
            InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight,
            PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords
    ) {
        float finalAttack = dualist$frameComputed ? dualist$offHandAttackOverride : attack;
        float finalInverseArmHeight = dualist$frameComputed
                ? Math.max(inverseArmHeight, dualist$offhandInverseArmHeightOverride)
                : inverseArmHeight;

        InteractionHand originalSwingingArm = player.swingingArm;
        boolean originalSwinging = player.swinging;

        if (finalAttack > 0) {
            player.swingingArm = InteractionHand.OFF_HAND;
            player.swinging = true;
        }

        try {
            ((ItemInHandRendererInvoker) self).dualist$invokeRenderArmWithItem(
                    player, frameInterp, xRot, InteractionHand.OFF_HAND, finalAttack,
                    itemStack, finalInverseArmHeight, poseStack, submitNodeCollector, lightCoords
            );
        } finally {
            player.swingingArm = originalSwingingArm;
            player.swinging = originalSwinging;
        }
    }
}