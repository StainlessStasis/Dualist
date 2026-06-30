package io.github.stainlessstasis.dualist;

import io.github.stainlessstasis.dualist.network.OffhandAttackPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(ModConstants.MOD_ID)
@EventBusSubscriber
public class ExampleMod {
    public ExampleMod(IEventBus eventBus) {}

    @SubscribeEvent
    public static void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                OffhandAttackPacket.TYPE,
                OffhandAttackPacket.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> OffhandAttack.perform(context.player(), payload.entityId(), payload.isMiss(), payload.attackStrengthTicker()));
                }
        );
    }
}