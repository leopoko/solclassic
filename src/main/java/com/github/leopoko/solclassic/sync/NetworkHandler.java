package com.github.leopoko.solclassic.sync;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("foodvariation", "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        int id = 0;
        CHANNEL.registerMessage(id++, SyncFoodHistoryPacket.class, SyncFoodHistoryPacket::encode, SyncFoodHistoryPacket::decode, SyncFoodHistoryPacket::handle);
    }
}
