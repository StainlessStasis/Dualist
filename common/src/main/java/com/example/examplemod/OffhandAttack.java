package com.example.examplemod;

import com.example.examplemod.mixin.LivingEntityAccessor;
import com.example.examplemod.network.OffhandAttackPacket;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.List;

public class OffhandAttack {
    public static void perform(Player player, int entityID, boolean isMiss) {
        if (player.level().isClientSide()) return;
        if (player.isSpectator()) return;
//        if (player.isUsingItem()) return;

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.isEmpty()) return;
        if (!offhand.isItemEnabled(player.level().enabledFeatures())) return;
        if (player.cannotAttackWithItem(offhand, 0)) return;

        if (isMiss) {
            player.resetAttackStrengthTicker();
            return;
        }

        if (entityID == OffhandAttackPacket.NO_ENTITY) return;
        Entity target = player.level().getEntity(entityID);
        if (target == null) return;

        List<Pair<Holder<Attribute>, AttributeModifier>> modifiersToApply = new ArrayList<>();
        offhand.forEachModifier(EquipmentSlot.MAINHAND, (attr, modifier) -> {
            modifiersToApply.add(Pair.of(attr, modifier));
        });

        for (Pair<Holder<Attribute>, AttributeModifier> pair : modifiersToApply) {
            AttributeInstance instance = player.getAttributes().getInstance(pair.getFirst());
            if (instance != null) {
                instance.removeModifier(pair.getSecond().id());
            }
        }
        for (Pair<Holder<Attribute>, AttributeModifier> pair : modifiersToApply) {
            AttributeInstance instance = player.getAttributes().getInstance(pair.getFirst());
            if (instance != null) {
                instance.addTransientModifier(pair.getSecond());
            }
        }

        player.resetAttackStrengthTicker();

        ItemStack mainhand = player.getItemInHand(InteractionHand.MAIN_HAND);
        player.setItemInHand(InteractionHand.MAIN_HAND, offhand);
        player.setItemInHand(InteractionHand.OFF_HAND, mainhand);

        IOffhandEntity offhandEntity = (IOffhandEntity)player;
        offhandEntity.examplemod$setPerformingOffhandAttack(true);
        ((LivingEntityAccessor)player).examplemod$setAttackStrengthTicker(Integer.MAX_VALUE);
        try {
            player.attack(target);
        } finally {
            offhandEntity.examplemod$setPerformingOffhandAttack(false);
            player.setItemInHand(InteractionHand.MAIN_HAND, mainhand);
            player.setItemInHand(InteractionHand.OFF_HAND, offhand);

            for (Pair<Holder<Attribute>, AttributeModifier> pair : modifiersToApply) {
                AttributeInstance instance = player.getAttributes().getInstance(pair.getFirst());
                if (instance != null) {
                    instance.removeModifier(pair.getSecond().id());
                }
            }
        }
    }
}