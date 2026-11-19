package com.realharvest.network.packet;

import com.realharvest.blueprint.Blueprint;
import com.realharvest.blueprint.BlueprintRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class C2SPlaceBlueprintPacket {
    private final String blueprintId;
    private final BlockPos origin;
    private final int rotation;

    public C2SPlaceBlueprintPacket(String blueprintId, BlockPos origin, int rotation) {
        this.blueprintId = blueprintId;
        this.origin = origin;
        this.rotation = rotation;
    }

    public static void encode(C2SPlaceBlueprintPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.blueprintId);
        buf.writeBlockPos(msg.origin);
        buf.writeVarInt(msg.rotation);
    }

    public static C2SPlaceBlueprintPacket decode(FriendlyByteBuf buf) {
        return new C2SPlaceBlueprintPacket(buf.readUtf(), buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(C2SPlaceBlueprintPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ServerLevel level = player.serverLevel();
            Blueprint bp = BlueprintRegistry.get(msg.blueprintId);
            if (bp != null && player.blockPosition().distSqr(msg.origin) <= 144) {
                for (Map.Entry<BlockPos, BlockState> entry : bp.getRotatedBlocks(msg.rotation).entrySet()) {
                    BlockPos target = msg.origin.offset(entry.getKey());
                    BlockState existing = level.getBlockState(target);
                    if (existing.isAir() || existing.canBeReplaced()) {
                        level.setBlock(target, entry.getValue(), Block.UPDATE_ALL);
                        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, entry.getValue()),
                                target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                                4, 0.25, 0.25, 0.25, 0.05);
                    }
                }
                level.playSound(null, msg.origin, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.5F, 0.9F);
            }
        }
        ctx.get().setPacketHandled(true);
    }
}
