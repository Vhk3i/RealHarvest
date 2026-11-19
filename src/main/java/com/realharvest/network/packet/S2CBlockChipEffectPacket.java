package com.realharvest.network.packet;

import com.realharvest.client.FirstPersonChopAnimator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CBlockChipEffectPacket {
    private final BlockPos pos;
    private final int blockStateId;

    public S2CBlockChipEffectPacket(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.blockStateId = Block.getId(state);
    }

    private S2CBlockChipEffectPacket(BlockPos pos, int blockStateId) {
        this.pos = pos;
        this.blockStateId = blockStateId;
    }

    public static void encode(S2CBlockChipEffectPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.blockStateId);
    }

    public static S2CBlockChipEffectPacket decode(FriendlyByteBuf buf) {
        return new S2CBlockChipEffectPacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(S2CBlockChipEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                BlockState state = Block.stateById(msg.blockStateId);
                mc.level.addDestroyBlockEffect(msg.pos, state);
                FirstPersonChopAnimator.triggerImpact();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
