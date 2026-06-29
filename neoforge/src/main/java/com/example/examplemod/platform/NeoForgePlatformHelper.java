package com.example.examplemod.platform;

import com.example.examplemod.network.OffhandAttackPacket;
import com.example.examplemod.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public void sendOffhandAttackPacket(int entityID, boolean isMiss) {
        ClientPacketDistributor.sendToServer(new OffhandAttackPacket(entityID, isMiss));
    }
}