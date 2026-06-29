package com.example.examplemod.mixin;

import com.example.examplemod.api.IOffhandRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.SwingAnimationType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements IOffhandRenderState {
    @Unique private float examplemod$offhandAttackAnim = 0f;
    @Unique private boolean examplemod$offhandSwinging = false;
    @Unique private SwingAnimationType examplemod$swingAnimType = SwingAnimationType.NONE;

    @Override
    public float examplemod$getOffhandAttackAnim() {
        return examplemod$offhandAttackAnim;
    }

    @Override
    public void examplemod$setOffhandAttackAnim(float value) {
        examplemod$offhandAttackAnim = value;
    }

    @Override
    public boolean examplemod$isOffhandSwinging() {
        return examplemod$offhandSwinging;
    }

    @Override
    public void examplemod$setOffhandSwinging(boolean value) {
        examplemod$offhandSwinging = value;
    }

    @Override
    public SwingAnimationType examplemod$getOffhandSwingAnimationType() {
        return examplemod$swingAnimType;
    }

    @Override
    public void examplemod$setOffhandSwingAnimationType(SwingAnimationType type) {
        examplemod$swingAnimType = type;
    }
}