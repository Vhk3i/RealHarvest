package com.realharvest.tree;

import com.realharvest.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class TreeDetector {
    public static final int MAX_BLOCKS = 256;
    private static final int MAX_HORIZONTAL_RADIUS = 7;

    public static Map<BlockPos, BlockState> detectTree(ServerLevel level, BlockPos cutPos, BlockState cutOriginalState) {
        if (!hasNaturalBase(level, cutPos)) {
            return Collections.emptyMap();
        }

        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        result.put(cutPos, cutOriginalState);
        queue.add(cutPos);
        visited.add(cutPos);

        int leafCount = 0;

        while (!queue.isEmpty() && result.size() < MAX_BLOCKS) {
            BlockPos current = queue.poll();
            BlockState currentState = result.get(current);
            boolean currentIsLog = isLog(currentState);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;

                        BlockPos next = current.offset(dx, dy, dz);
                        if (visited.contains(next)) continue;
                        if (Math.abs(next.getX() - cutPos.getX()) > MAX_HORIZONTAL_RADIUS
                                || Math.abs(next.getZ() - cutPos.getZ()) > MAX_HORIZONTAL_RADIUS) {
                            continue;
                        }

                        BlockState nextState = level.getBlockState(next);
                        if (isLog(nextState)) {
                            visited.add(next);
                            result.put(next, nextState);
                            queue.add(next);
                        } else if (isNaturalLeaf(nextState)) {
                            if (currentIsLog || current.distManhattan(cutPos) <= MAX_HORIZONTAL_RADIUS + 4) {
                                visited.add(next);
                                result.put(next, nextState);
                                queue.add(next);
                                leafCount++;
                            }
                        }

                        if (result.size() >= MAX_BLOCKS) break;
                    }
                    if (result.size() >= MAX_BLOCKS) break;
                }
                if (result.size() >= MAX_BLOCKS) break;
            }
        }

        if (leafCount == 0 && result.size() < 3) {
            return Collections.emptyMap();
        }

        return result;
    }

    private static boolean hasNaturalBase(ServerLevel level, BlockPos cutPos) {
        BlockPos cursor = cutPos.below();
        for (int i = 0; i < 6; i++) {
            BlockState state = level.getBlockState(cursor);
            if (state.is(BlockTags.DIRT)) {
                return true;
            }
            if (!isLog(state) && !state.is(ModBlocks.NOTCHED_LOG.get())) {
                return false;
            }
            cursor = cursor.below();
        }
        return false;
    }

    private static boolean isLog(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    private static boolean isNaturalLeaf(BlockState state) {
        if (!state.is(BlockTags.LEAVES)) return false;
        if (state.hasProperty(LeavesBlock.PERSISTENT) && state.getValue(LeavesBlock.PERSISTENT)) {
            return false;
        }
        return true;
    }
}
