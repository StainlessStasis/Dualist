package com.example.examplemod.mixin;

import com.example.examplemod.api.IHandRenderSelection;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "ItemInHandRenderer$HandRenderSelection")
public interface HandRenderSelectionAccessor extends IHandRenderSelection {

    @Override
    @Accessor("renderMainHand")
    boolean examplemod$renderMainHand();

    @Override
    @Accessor("renderOffHand")
    boolean examplemod$renderOffHand();

    @Invoker("evaluateWhichHandsToRender")
    static Object examplemod$invokeEvaluateWhichHandsToRender(LocalPlayer player) {
        throw new UnsupportedOperationException();
    }
}