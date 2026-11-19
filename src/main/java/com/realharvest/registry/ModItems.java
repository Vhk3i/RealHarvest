package com.realharvest.registry;

import com.realharvest.RealHarvest;
import com.realharvest.item.LighterItem;
import com.realharvest.item.MolotovItem;
import com.realharvest.item.SurvivalGuideItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RealHarvest.MOD_ID);

    public static final RegistryObject<Item> STONE_PEBBLE =
            ITEMS.register("stone_pebble", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_IRON_NUGGET =
            ITEMS.register("raw_iron_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_GOLD_NUGGET =
            ITEMS.register("raw_gold_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_COPPER_NUGGET =
            ITEMS.register("raw_copper_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COAL_FRAGMENT =
            ITEMS.register("coal_fragment", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CANNIBAL_SPAWN_EGG =
            ITEMS.register("cannibal_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.CANNIBAL, 0x7A736A, 0x7D1212, new Item.Properties()));

    public static final RegistryObject<Item> SURVIVAL_GUIDE =
            ITEMS.register("survival_guide", () -> new SurvivalGuideItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> LIGHTER =
            ITEMS.register("lighter", () -> new LighterItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> MOLOTOV =
            ITEMS.register("molotov", () -> new MolotovItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> SPIKE_TRAP =
            ITEMS.register("spike_trap", () -> new BlockItem(ModBlocks.SPIKE_TRAP.get(), new Item.Properties()));

    public static final RegistryObject<Item> EFFIGY =
            ITEMS.register("effigy", () -> new BlockItem(ModBlocks.EFFIGY.get(), new Item.Properties()));
}
