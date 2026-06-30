package io.github.stainlessstasis.dualist.mixin;

import io.github.stainlessstasis.dualist.api.IOffhandEntity;
import io.github.stainlessstasis.dualist.api.IOffhandRenderState;
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
    private <E extends Avatar & ClientAvatarEntity> void dualist$extractOffhandSwing(
            E entity, AvatarRenderState state, float partialTicks, CallbackInfo ci
    ) {
        if (!(entity instanceof IOffhandEntity offhandEntity)) return;

        IOffhandRenderState offhandState = (IOffhandRenderState) state;
        offhandState.dualist$setOffhandAttackAnim(offhandEntity.dualist$getOffhandAttackAnim(partialTicks));
        offhandState.dualist$setOffhandSwinging(offhandEntity.dualist$isOffhandSwinging());
        ItemStack offhand = entity.getItemInHand(InteractionHand.OFF_HAND);
        offhandState.dualist$setOffhandSwingAnimationType(offhand.getSwingAnimation().type());
    }
}