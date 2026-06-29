package com.example.examplemod.mixin;

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
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private void examplemod$submitBothHandsIndependently(
            float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            LocalPlayer player, int lightCoords, CallbackInfo ci
    ) {
        IOffhandEntity offhandEntity = (IOffhandEntity) player;

        float mainHandAttack;
        float offHandAttack;

        boolean mainHandSwinging = player.swingingArm == InteractionHand.MAIN_HAND && player.swinging;
        boolean offHandSwinging  = offhandEntity.examplemod$isOffhandSwinging();

        if (mainHandSwinging && offHandSwinging) {
            mainHandAttack = player.getAttackAnim(frameInterp);
            offHandAttack  = offhandEntity.examplemod$getOffhandAttackAnim(frameInterp);
        } else if (offHandSwinging) {
            mainHandAttack = 0f;
            offHandAttack  = offhandEntity.examplemod$getOffhandAttackAnim(frameInterp);
        } else {

            mainHandAttack = (player.swingingArm == InteractionHand.MAIN_HAND || !player.swinging)
                    ? player.getAttackAnim(frameInterp) : 0f;
            offHandAttack = 0f;
        }

        boolean renderMainHand;
        boolean renderOffHand;

        ItemStack liveMain = player.getMainHandItem();
        ItemStack liveOffhand = player.getOffhandItem();
        boolean holdsBow = liveMain.is(Items.BOW) || liveOffhand.is(Items.BOW);
        boolean holdsCrossbow = liveMain.is(Items.CROSSBOW) || liveOffhand.is(Items.CROSSBOW);

        if (!holdsBow && !holdsCrossbow) {
            renderMainHand = true;
            renderOffHand  = true;
        } else if (player.isUsingItem()) {
            ItemStack usedItem = player.getUseItem();
            InteractionHand usedHand = player.getUsedItemHand();
            if (!usedItem.is(Items.BOW) && !usedItem.is(Items.CROSSBOW)) {
                boolean offhandCharged = liveOffhand.is(Items.CROSSBOW) && CrossbowItem.isCharged(liveOffhand);
                renderMainHand = true;
                renderOffHand = !(usedHand == InteractionHand.MAIN_HAND && offhandCharged);
            } else {
                renderMainHand = usedHand == InteractionHand.MAIN_HAND;
                renderOffHand = usedHand == InteractionHand.OFF_HAND;
            }
        } else {
            boolean mainCharged = liveMain.is(Items.CROSSBOW) && CrossbowItem.isCharged(liveMain);
            renderMainHand = true;
            renderOffHand = !mainCharged;
        }

        float xRot = player.getXRot(frameInterp);
        float xBob = Mth.lerp(frameInterp, player.xBobO, player.xBob);
        float yBob = Mth.lerp(frameInterp, player.yBobO, player.yBob);
        poseStack.mulPose(Axis.XP.rotationDegrees((player.getViewXRot(frameInterp) - xBob) * 0.1F));
        poseStack.mulPose(Axis.YP.rotationDegrees((player.getViewYRot(frameInterp) - yBob) * 0.1F));

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
            examplemod$invokeSubmitArmWithItem(
                    player, frameInterp, xRot,
                    InteractionHand.OFF_HAND, offHandAttack,
                    this.offHandItem, offhandInverseArmHeight,
                    poseStack, submitNodeCollector, lightCoords
            );
        }

        ci.cancel();
    }
}