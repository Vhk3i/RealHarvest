package com.realharvest.network;

import com.realharvest.RealHarvest;
import com.realharvest.network.packet.S2CBlockChipEffectPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RealHarvest.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(S2CBlockChipEffectPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CBlockChipEffectPacket::encode)
                .decoder(S2CBlockChipEffectPacket::decode)
                .consumerMainThread(S2CBlockChipEffectPacket::handle)
                .add();
    }

    public static void sendToTracking(ServerLevel level, BlockPos pos, Object packet) {
        CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
    }
}
