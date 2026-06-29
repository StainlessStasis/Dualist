package com.example.examplemod.network;

import com.example.examplemod.ModConstants;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.NonNull;

public record OffhandAttackPacket() implements CustomPacketPayload {
    public static final Type<OffhandAttackPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "offhand_attack"));

    public static final StreamCodec<ByteBuf, OffhandAttackPacket> STREAM_CODEC =
            StreamCodec.unit(new OffhandAttackPacket());

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
