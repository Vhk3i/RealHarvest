package com.realharvest.registry;

import com.realharvest.RealHarvest;
import net.minecraft.world.item.Item;
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
}
