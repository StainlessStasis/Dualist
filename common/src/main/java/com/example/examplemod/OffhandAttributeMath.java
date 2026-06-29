package com.example.examplemod;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OffhandAttributeMath {
    public static double resolveAttributes(Player player, Holder<Attribute> attribute, ItemStack offhandStack) {
        AttributeInstance instance = player.getAttributes().getInstance(attribute);
        if (instance == null) return 0;

        double baseValue = instance.getBaseValue();

        ItemStack mainhand = player.getItemInHand(InteractionHand.MAIN_HAND);
        Set<Identifier> mainhandIds = new HashSet<>();
        if (!mainhand.isEmpty()) {
            mainhand.forEachModifier(EquipmentSlot.MAINHAND, (attr, mod) -> {
                if (attr.value() == attribute.value()) {
                    mainhandIds.add(mod.id());
                }
            });
        }

        List<AttributeModifier> otherAdd = new ArrayList<>();
        List<AttributeModifier> otherMultBase = new ArrayList<>();
        List<AttributeModifier> otherMultTotal = new ArrayList<>();

        for (AttributeModifier mod : instance.getModifiers()) {
            if (mainhandIds.contains(mod.id())) continue;
            switch (mod.operation()) {
                case ADD_VALUE -> otherAdd.add(mod);
                case ADD_MULTIPLIED_BASE -> otherMultBase.add(mod);
                case ADD_MULTIPLIED_TOTAL -> otherMultTotal.add(mod);
            }
        }

        List<AttributeModifier> offhandAdd = new ArrayList<>();
        List<AttributeModifier> offhandMultBase = new ArrayList<>();
        List<AttributeModifier> offhandMultTotal = new ArrayList<>();

        if (!offhandStack.isEmpty()) {
            offhandStack.forEachModifier(EquipmentSlot.MAINHAND, (att, mod) -> {
                if (att.value() != attribute.value()) return;
                switch (mod.operation()) {
                    case ADD_VALUE -> offhandAdd.add(mod);
                    case ADD_MULTIPLIED_BASE -> offhandMultBase.add(mod);
                    case ADD_MULTIPLIED_TOTAL -> offhandMultTotal.add(mod);
                }
            });
        }

        double addSum = 0;
        for (AttributeModifier m : otherAdd) addSum += m.amount();
        for (AttributeModifier m : offhandAdd) addSum += m.amount();
        double result = baseValue + addSum;

        double multBaseSum = 0;
        for (AttributeModifier m : otherMultBase) multBaseSum += m.amount();
        for (AttributeModifier m : offhandMultBase) multBaseSum += m.amount();
        result += result * multBaseSum;

        for (AttributeModifier m : otherMultTotal) result *= (1 + m.amount());
        for (AttributeModifier m : offhandMultTotal) result *= (1 + m.amount());

        result = attribute.value().sanitizeValue(result);

        return result;
    }
}