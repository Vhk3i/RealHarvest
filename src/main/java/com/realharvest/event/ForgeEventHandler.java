package com.realharvest.event;

import com.realharvest.RealHarvest;
import com.realharvest.entity.FallingTreeEntity;
import com.realharvest.mining.ChippedBlockEntity;
import com.realharvest.mining.VoxelShapeCache;
import com.realharvest.network.ModNetwork;
import com.realharvest.network.packet.S2CBlockChipEffectPacket;
import com.realharvest.registry.ModBlocks;
import com.realharvest.tree.NotchedLogBlockEntity;
import com.realharvest.tree.TreeDetector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = RealHarvest.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        Player player = event.getPlayer();
        if (player == null || player.isCreative()) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        ItemStack held = player.getMainHandItem();

        if (held.getItem() instanceof AxeItem) {
            if (state.is(BlockTags.LOGS) || state.is(ModBlocks.NOTCHED_LOG.get())) {
                handleTreeChop(event, level, player, pos, state, held);
                return;
            }
        }

        if (held.getItem() instanceof PickaxeItem) {
            boolean isStoneOrOre = state.is(Tags.Blocks.STONE)
                    || state.is(BlockTags.BASE_STONE_OVERWORLD)
                    || state.is(Tags.Blocks.ORES)
                    || state.is(ModBlocks.CHIPPED_BLOCK.get());
            if (isStoneOrOre) {
                handleStoneChip(event, level, player, pos, state, held);
            }
        }
    }

    private static void handleTreeChop(BlockEvent.BreakEvent event, ServerLevel level, Player player,
                                       BlockPos pos, BlockState state, ItemStack axe) {
        NotchedLogBlockEntity be;
        BlockState original;

        if (!state.is(ModBlocks.NOTCHED_LOG.get())) {
            original = state;
            level.setBlock(pos, ModBlocks.NOTCHED_LOG.get().defaultBlockState(), Block.UPDATE_ALL);
            if (!(level.getBlockEntity(pos) instanceof NotchedLogBlockEntity created)) return;
            created.setOriginalState(original);
            be = created;
        } else {
            if (!(level.getBlockEntity(pos) instanceof NotchedLogBlockEntity existing)) return;
            be = existing;
            original = be.getOriginalState();
        }

        Direction hitSide = player.getDirection().getOpposite();
        boolean felled = be.addChop(hitSide, 1);

        axe.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        ModNetwork.sendToTracking(level, pos, new S2CBlockChipEffectPacket(pos, original));

        if (!felled) {
            event.setCanceled(true);
            player.displayClientMessage(Component.literal(
                    "Зарубка: " + be.getTotalChops() + "/" + NotchedLogBlockEntity.MAX_CHOPS), true);
        } else {
            Map<BlockPos, BlockState> treeBlocks = TreeDetector.detectTree(level, pos, original);
            if (!treeBlocks.isEmpty()) {
                event.setCanceled(true);
                for (BlockPos blockPos : treeBlocks.keySet()) {
                    level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
                Direction fallDir = hitSide.getOpposite();
                FallingTreeEntity entity = new FallingTreeEntity(level, pos, treeBlocks, fallDir);
                level.addFreshEntity(entity);
            }
        }
    }

    private static void handleStoneChip(BlockEvent.BreakEvent event, ServerLevel level, Player player,
                                        BlockPos pos, BlockState state, ItemStack pickaxe) {
        ChippedBlockEntity be;
        BlockState original;

        if (!state.is(ModBlocks.CHIPPED_BLOCK.get())) {
            original = state;
            level.setBlock(pos, ModBlocks.CHIPPED_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
            if (!(level.getBlockEntity(pos) instanceof ChippedBlockEntity created)) return;
            created.setOriginalState(original);
            be = created;
        } else {
            if (!(level.getBlockEntity(pos) instanceof ChippedBlockEntity existing)) return;
            be = existing;
            original = be.getOriginalState();
        }

        int octant = calculateTargetOctant(level, player, pos);
        boolean destroyed = be.chipOctant(octant);

        pickaxe.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        ModNetwork.sendToTracking(level, pos, new S2CBlockChipEffectPacket(pos, original));

        if (!destroyed) {
            event.setCanceled(true);
            int remaining = Integer.bitCount(be.getVoxelMask());
            player.displayClientMessage(Component.literal("Осталось частей камня: " + remaining + "/8"), true);
        }
    }

    private static int calculateTargetOctant(ServerLevel level, Player player, BlockPos pos) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(6.0));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(pos)) {
            Vec3 rel = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
            int x = rel.x >= 0.5 ? 1 : 0;
            int y = rel.y >= 0.5 ? 1 : 0;
            int z = rel.z >= 0.5 ? 1 : 0;
            return VoxelShapeCache.getBitIndex(x, y, z);
        }
        return 0;
    }
}
