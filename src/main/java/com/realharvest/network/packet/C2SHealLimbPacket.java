package com.realharvest.network.packet;

import com.realharvest.health.BodyHealthManager;
import com.realharvest.health.BodyPart;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SHealLimbPacket {
    private final int partOrdinal;

    public C2SHealLimbPacket(BodyPart part) {
        this.partOrdinal = part.ordinal();
    }

    private C2SHealLimbPacket(int partOrdinal) {
        this.partOrdinal = partOrdinal;
    }

    public static void encode(C2SHealLimbPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.partOrdinal);
    }

    public static C2SHealLimbPacket decode(FriendlyByteBuf buf) {
        return new C2SHealLimbPacket(buf.readVarInt());
    }

    public static void handle(C2SHealLimbPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            BodyPart[] parts = BodyPart.values();
            if (msg.partOrdinal >= 0 && msg.partOrdinal < parts.length) {
                BodyHealthManager.tryHealLimb(player, parts[msg.partOrdinal]);
            }
        }
        ctx.get().setPacketHandled(true);
    }
}
