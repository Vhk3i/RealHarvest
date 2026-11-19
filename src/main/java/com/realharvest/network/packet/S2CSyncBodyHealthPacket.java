package com.realharvest.network.packet;

import com.realharvest.client.health.ClientBodyHealth;
import com.realharvest.health.BodyPart;
import com.realharvest.health.InjuryType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncBodyHealthPacket {
    private final int[] states;

    public S2CSyncBodyHealthPacket(int[] states) {
        this.states = states;
    }

    public static void encode(S2CSyncBodyHealthPacket msg, FriendlyByteBuf buf) {
        buf.writeVarIntArray(msg.states);
    }

    public static S2CSyncBodyHealthPacket decode(FriendlyByteBuf buf) {
        return new S2CSyncBodyHealthPacket(buf.readVarIntArray());
    }

    public static void handle(S2CSyncBodyHealthPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            BodyPart[] parts = BodyPart.values();
            InjuryType[] types = InjuryType.values();
            for (int i = 0; i < Math.min(parts.length, msg.states.length); i++) {
                int idx = Math.max(0, Math.min(types.length - 1, msg.states[i]));
                ClientBodyHealth.set(parts[i], types[idx]);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
