package com.example.examplemod.mixin;

import com.example.examplemod.OffhandAttributeMath;
import com.example.examplemod.api.IOffhandEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerMixin {
    @Redirect(
            method = "isSweepAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack examplemod$redirectSweepItemCheck(Player player, InteractionHand hand) {
        IOffhandEntity offhandEntity = (IOffhandEntity) player;
        if (offhandEntity.examplemod$isPerformingOffhandAttack()) {
            return player.getItemInHand(InteractionHand.OFF_HAND);
        }
        return player.getItemInHand(hand);
    }

    @Redirect(
            method = "getCurrentItemAttackStrengthDelay",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D"
            )
    )
    private double examplemod$redirectAttackSpeedForDelay(Player player, Holder<Attribute> attribute) {
        double mainhandSpeed = player.getAttributeValue(attribute);
        if (attribute.value() != Attributes.ATTACK_SPEED.value()) {
            return mainhandSpeed;
        }

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.isEmpty()) {
            return mainhandSpeed;
        }
        double offhandSpeed = OffhandAttributeMath.resolveAttributes(player, Attributes.ATTACK_SPEED, offhand);

        return Math.min(mainhandSpeed, offhandSpeed);
    }
}