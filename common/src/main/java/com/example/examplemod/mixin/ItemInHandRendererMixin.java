package com.example.examplemod.mixin;

import com.example.examplemod.ModConstants;
import com.example.examplemod.api.IOffhandEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow private ItemStack mainHandItem;
    @Shadow private ItemStack offHandItem;
    @Shadow private float mainHandHeight;
    @Shadow private float oMainHandHeight;
    @Shadow private float offHandHeight;
    @Shadow private float oOffHandHeight;
    @Final @Shadow private ItemModelResolver itemModelResolver;

    @Invoker("submitArmWithItem")
    abstract void examplemod$invokeSubmitArmWithItem(
            AbstractClientPlayer player, float frameInterp, float xRot,
            InteractionHand hand, float attack, ItemStack itemStack,
            float inverseArmHeight, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, int lightCoords
    );

    @Inject(
            method = "submitHandsWithItems",
            at = @At("HEAD"),
            cancellable = true
    )
    private void examplemod$submitHands(
            float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            LocalPlayer player, int lightCoords, CallbackInfo ci
    ) {
        IOffhandEntity offhandEntity = (IOffhandEntity) player;

        float mainHandAttack = (player.swinging && player.swingingArm == InteractionHand.MAIN_HAND)
                ? player.getAttackAnim(frameInterp) : 0.0F;

        float offHandAttack = offhandEntity.examplemod$getOffhandAttackAnim(frameInterp);
        if (offHandAttack <= 0) {
            offHandAttack = 0;
        }

        float xRot = player.getXRot(frameInterp);
        float xBob = Mth.lerp(frameInterp, player.xBobO, player.xBob);
        float yBob = Mth.lerp(frameInterp, player.yBobO, player.yBob);
        poseStack.mulPose(Axis.XP.rotationDegrees((player.getViewXRot(frameInterp) - xBob) * 0.1F));
        poseStack.mulPose(Axis.YP.rotationDegrees((player.getViewYRot(frameInterp) - yBob) * 0.1F));

        boolean renderMainHand;
        boolean renderOffHand;
        try {
            Method m = ItemInHandRenderer.class.getDeclaredMethod(
                    "evaluateWhichHandsToRender", LocalPlayer.class
            );
            m.setAccessible(true);
            Object hrs = m.invoke(null, player);
            Class<?> hrsClass = hrs.getClass();
            renderMainHand = hrsClass.getDeclaredField("renderMainHand").getBoolean(hrs);
            renderOffHand = hrsClass.getDeclaredField("renderOffHand").getBoolean(hrs);
        } catch (Exception e) {
            renderMainHand = true;
            renderOffHand = false;
            ModConstants.LOG.error("Failed to evaluate which hands to render: {}. Defaulting to render main hand and not render offhand.", e.getLocalizedMessage());
        }
        if (renderMainHand) {
            float mainhandInverseArmHeight = this.itemModelResolver.swapAnimationScale(this.mainHandItem)
                    * (1.0F - Mth.lerp(frameInterp, this.oMainHandHeight, this.mainHandHeight));
            examplemod$invokeSubmitArmWithItem(
                    player, frameInterp, xRot,
                    InteractionHand.MAIN_HAND, mainHandAttack,
                    this.mainHandItem, mainhandInverseArmHeight,
                    poseStack, submitNodeCollector, lightCoords
            );
        }

        if (renderOffHand) {
            float offhandInverseArmHeight = this.itemModelResolver.swapAnimationScale(this.offHandItem)
                    * (1.0F - Mth.lerp(frameInterp, this.oOffHandHeight, this.offHandHeight));

            InteractionHand originalSwingingArm = player.swingingArm;
            boolean originalSwinging = player.swinging;

            if (offHandAttack > 0) {
                player.swingingArm = InteractionHand.OFF_HAND;
                player.swinging = true;
            }

            try {
                examplemod$invokeSubmitArmWithItem(
                        player, frameInterp, xRot,
                        InteractionHand.OFF_HAND, offHandAttack,
                        this.offHandItem, offhandInverseArmHeight,
                        poseStack, submitNodeCollector, lightCoords
                );
            } finally {
                player.swingingArm = originalSwingingArm;
                player.swinging = originalSwinging;
            }
        }

        ci.cancel();
    }
}