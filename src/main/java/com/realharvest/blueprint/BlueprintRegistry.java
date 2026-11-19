package com.realharvest.blueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class BlueprintRegistry {
    private static final Map<String, Blueprint> BLUEPRINTS = new LinkedHashMap<>();

    static {
        register(createHunterCamp());
        register(createLogCabin());
        register(createStoneHolder());
        register(createStickHolder());
    }

    private static void register(Blueprint bp) {
        BLUEPRINTS.put(bp.getId(), bp);
    }

    public static Collection<Blueprint> getAll() {
        return BLUEPRINTS.values();
    }

    public static Blueprint get(String id) {
        return BLUEPRINTS.get(id);
    }

    // 1. Лагерь охотника на дичь
    private static Blueprint createHunterCamp() {
        Map<BlockPos, BlockState> b = new LinkedHashMap<>();
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState slab = Blocks.SPRUCE_SLAB.defaultBlockState();
        BlockState fence = Blocks.SPRUCE_FENCE.defaultBlockState();

        // Опоры навеса
        for (int y = 0; y <= 2; y++) {
            b.put(new BlockPos(-1, y, -1), fence);
            b.put(new BlockPos(1, y, -1), fence);
            b.put(new BlockPos(-1, y, 1), log);
            b.put(new BlockPos(1, y, 1), log);
        }
        // Крыша навеса
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                b.put(new BlockPos(x, 3, z), slab);
            }
        }
        // Костёр перед лагерем и сундук для трофеев
        b.put(new BlockPos(0, 0, -2), Blocks.CAMPFIRE.defaultBlockState());
        b.put(new BlockPos(0, 0, 0), Blocks.CHEST.defaultBlockState());
        b.put(new BlockPos(-2, 0, -1), Blocks.CRAFTING_TABLE.defaultBlockState());

        return new Blueprint("hunter_camp", "Лагерь охотника", "Навес + костёр",
                new ItemStack(Items.CAMPFIRE), b);
    }

    // 2. Бревенчатый дом (сруб 5x5)
    private static Blueprint createLogCabin() {
        Map<BlockPos, BlockState> b = new LinkedHashMap<>();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState slab = Blocks.SPRUCE_SLAB.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();

        // Пол
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                b.put(new BlockPos(x, 0, z), planks);
            }
        }
        // Стены (высота 3 блока)
        for (int y = 1; y <= 3; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    boolean edge = (x == -2 || x == 2 || z == -2 || z == 2);
                    if (!edge) continue;
                    // Проход для двери спереди (z = -2, x = 0, y = 1..2)
                    if (z == -2 && x == 0 && y <= 2) continue;
                    // Окна по бокам
                    if ((x == -2 || x == 2) && z == 0 && y == 2) {
                        b.put(new BlockPos(x, y, z), glass);
                        continue;
                    }
                    b.put(new BlockPos(x, y, z), log);
                }
            }
        }
        // Крыша
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                b.put(new BlockPos(x, 4, z), slab);
            }
        }

        return new Blueprint("log_cabin", "Бревенчатый дом", "Сруб 5x5",
                new ItemStack(Items.OAK_LOG), b);
    }

    // 3. Склад для камня
    private static Blueprint createStoneHolder() {
        Map<BlockPos, BlockState> b = new LinkedHashMap<>();
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState slab = Blocks.COBBLESTONE_SLAB.defaultBlockState();

        b.put(new BlockPos(-1, 0, -1), fence);
        b.put(new BlockPos(1, 0, -1), fence);
        b.put(new BlockPos(-1, 0, 1), fence);
        b.put(new BlockPos(1, 0, 1), fence);

        b.put(new BlockPos(0, 0, 0), cobble);
        b.put(new BlockPos(0, 0, -1), cobble);
        b.put(new BlockPos(0, 0, 1), cobble);
        b.put(new BlockPos(-1, 0, 0), cobble);
        b.put(new BlockPos(1, 0, 0), cobble);
        b.put(new BlockPos(0, 1, 0), slab);

        return new Blueprint("stone_holder", "Склад для камня", "Корзина камней",
                new ItemStack(Items.COBBLESTONE), b);
    }

    // 4. Склад для веток
    private static Blueprint createStickHolder() {
        Map<BlockPos, BlockState> b = new LinkedHashMap<>();
        BlockState fence = Blocks.SPRUCE_FENCE.defaultBlockState();
        BlockState trapdoor = Blocks.SPRUCE_TRAPDOOR.defaultBlockState();
        BlockState slab = Blocks.OAK_SLAB.defaultBlockState();

        b.put(new BlockPos(-1, 0, 0), fence);
        b.put(new BlockPos(1, 0, 0), fence);
        b.put(new BlockPos(-1, 1, 0), fence);
        b.put(new BlockPos(1, 1, 0), fence);
        b.put(new BlockPos(0, 0, 0), slab);
        b.put(new BlockPos(0, 1, 0), trapdoor);

        return new Blueprint("stick_holder", "Склад для веток", "Стойка палок",
                new ItemStack(Items.STICK), b);
    }
}
