package com.example.examplemod;

import com.example.examplemod.network.OffhandAttackPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class OffhandAttack {
    public static void perform(Player player, int entityID, boolean isMiss) {
        if (player.level().isClientSide()) return;
        if (player.isSpectator()) return;
//        if (player.isHandsBusy()) return;

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.isEmpty()) return;
        if (!offhand.isItemEnabled(player.level().enabledFeatures())) return;
        if (player.cannotAttackWithItem(offhand, 0)) return;

        if (isMiss) {
            player.resetAttackStrengthTicker();
            player.swing(InteractionHand.OFF_HAND);
            return;
        }

        if (entityID == OffhandAttackPacket.NO_ENTITY) return;

        Entity target = player.level().getEntity(entityID);
        if (target == null) return;

        IOffhandEntity offhandEntity = (IOffhandEntity)player;
        offhandEntity.examplemod$setPerformingOffhandAttack(true);
        try {
            System.out.println("attack");
            player.attack(target);
        } finally {
            offhandEntity.examplemod$setPerformingOffhandAttack(false);
        }

        player.swing(InteractionHand.OFF_HAND);
    }
}