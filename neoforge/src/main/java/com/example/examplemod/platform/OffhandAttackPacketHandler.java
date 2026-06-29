package com.example.examplemod.platform;

import com.example.examplemod.OffhandAttack;
import com.example.examplemod.network.OffhandAttackPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class OffhandAttackPacketHandler {
    public static void handle(OffhandAttackPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> OffhandAttack.perform((ServerPlayer) context.player()));
    }
}
