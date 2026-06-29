package com.example.examplemod;

import com.example.examplemod.network.OffhandAttackPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ExampleMod implements ModInitializer {

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(OffhandAttackPacket.TYPE, OffhandAttackPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(OffhandAttackPacket.TYPE, (payload, context) -> {
            context.server().execute(() -> OffhandAttack.perform(context.player()));
        });
    }
}
