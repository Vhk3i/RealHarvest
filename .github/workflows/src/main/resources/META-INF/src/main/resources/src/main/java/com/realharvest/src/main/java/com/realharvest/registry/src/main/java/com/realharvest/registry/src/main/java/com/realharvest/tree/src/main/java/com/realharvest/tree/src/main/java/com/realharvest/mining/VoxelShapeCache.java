package com.realharvest.mining;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoxelShapeCache {
    private static final VoxelShape[] SHAPES = new VoxelShape[256];

    static {
        for (int mask = 0; mask < 256; mask++) {
            VoxelShape shape = Shapes.empty();
            for (int bit = 0; bit < 8; bit++) {
                if ((mask & (1 << bit)) != 0) {
                    int x = bit & 1;
                    int y = (bit >> 1) & 1;
                    int z = (bit >> 2) & 1;
                    shape = Shapes.or(shape, Shapes.box(
                            x * 0.5, y * 0.5, z * 0.5,
                            (x + 1) * 0.5, (y + 1) * 0.5, (z + 1) * 0.5
                    ));
                }
            }
            SHAPES[mask] = shape.optimize();
        }
    }

    public static VoxelShape getShape(int mask) {
        return SHAPES[mask & 0xFF];
    }

    public static int getBitIndex(int x, int y, int z) {
        return (x & 1) | ((y & 1) << 1) | ((z & 1) << 2);
    }
}
