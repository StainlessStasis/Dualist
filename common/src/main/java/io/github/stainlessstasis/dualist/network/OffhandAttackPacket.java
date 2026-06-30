package io.github.stainlessstasis.dualist.network;

import io.github.stainlessstasis.dualist.DualistConstants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.NonNull;

public record OffhandAttackPacket(int entityId, boolean isMiss, int attackStrengthTicker) implements CustomPacketPayload {
    public static final int NO_ENTITY = -1;

    public static final Type<OffhandAttackPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(DualistConstants.MOD_ID, "offhand_attack"));

    public static final StreamCodec<ByteBuf, OffhandAttackPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, OffhandAttackPacket::entityId,
            ByteBufCodecs.BOOL, OffhandAttackPacket::isMiss,
            ByteBufCodecs.INT, OffhandAttackPacket::attackStrengthTicker,
            OffhandAttackPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
