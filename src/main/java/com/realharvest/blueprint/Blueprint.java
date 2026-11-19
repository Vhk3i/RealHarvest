package com.realharvest.blueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;

public class Blueprint {
    private final String id;
    private final String title;
    private final String subtitle;
    private final ItemStack icon;
    private final Map<BlockPos, BlockState> blocks;

    public Blueprint(String id, String title, String subtitle, ItemStack icon, Map<BlockPos, BlockState> blocks) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.icon = icon;
        this.blocks = blocks;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public ItemStack getIcon() { return icon; }
    public Map<BlockPos, BlockState> getBlocks() { return blocks; }

    public Map<BlockPos, BlockState> getRotatedBlocks(int rotationSteps) {
        int steps = Math.floorMod(rotationSteps, 4);
        Rotation rot = switch (steps) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };

        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos p = entry.getKey();
            BlockPos rp = switch (steps) {
                case 1 -> new BlockPos(-p.getZ(), p.getY(), p.getX());
                case 2 -> new BlockPos(-p.getX(), p.getY(), -p.getZ());
                case 3 -> new BlockPos(p.getZ(), p.getY(), -p.getX());
                default -> p;
            };
            result.put(rp, entry.getValue().rotate(rot));
        }
        return result;
    }
}
