package com.example.examplemod.mixin;

import com.example.examplemod.api.IOffhandEntity;
import com.example.examplemod.api.IOffhandRenderState;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL")
    )
    private <E extends Avatar & ClientAvatarEntity> void examplemod$extractOffhandSwing(
            E entity, AvatarRenderState state, float partialTicks, CallbackInfo ci
    ) {
        if (!(entity instanceof IOffhandEntity offhandEntity)) return;

        IOffhandRenderState offhandState = (IOffhandRenderState) state;
        offhandState.examplemod$setOffhandAttackAnim(offhandEntity.examplemod$getOffhandAttackAnim(partialTicks));
        offhandState.examplemod$setOffhandSwinging(offhandEntity.examplemod$isOffhandSwinging());
        ItemStack offhand = entity.getItemInHand(InteractionHand.OFF_HAND);
        offhandState.examplemod$setOffhandSwingAnimationType(offhand.getSwingAnimation().type());
    }
}