package com.realharvest.registry;

import com.realharvest.RealHarvest;
import com.realharvest.armor.ModArmorMaterials;
import com.realharvest.item.LighterItem;
import com.realharvest.item.MolotovItem;
import com.realharvest.item.SurvivalGuideItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RealHarvest.MOD_ID);

    public static final RegistryObject<Item> STONE_PEBBLE = ITEMS.register("stone_pebble", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_IRON_NUGGET = ITEMS.register("raw_iron_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_GOLD_NUGGET = ITEMS.register("raw_gold_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_COPPER_NUGGET = ITEMS.register("raw_copper_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COAL_FRAGMENT = ITEMS.register("coal_fragment", () -> new Item(new Item.Properties()));

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
    public static final RegistryObject<Item> DRYING_RACK =
            ITEMS.register("drying_rack", () -> new BlockItem(ModBlocks.DRYING_RACK.get(), new Item.Properties()));

    public static final RegistryObject<Item> DRIED_MEAT =
            ITEMS.register("dried_meat", () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(8).saturationMod(0.9F).meat().build())));

    // Костяная броня
    public static final RegistryObject<Item> BONE_HELMET =
            ITEMS.register("bone_helmet", () -> new ArmorItem(ModArmorMaterials.BONE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> BONE_CHESTPLATE =
            ITEMS.register("bone_chestplate", () -> new ArmorItem(ModArmorMaterials.BONE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> BONE_LEGGINGS =
            ITEMS.register("bone_leggings", () -> new ArmorItem(ModArmorMaterials.BONE, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> BONE_BOOTS =
            ITEMS.register("bone_boots", () -> new ArmorItem(ModArmorMaterials.BONE, ArmorItem.Type.BOOTS, new Item.Properties()));

    // Лиственная броня (стелс)
    public static final RegistryObject<Item> LEAF_HELMET =
            ITEMS.register("leaf_helmet", () -> new ArmorItem(ModArmorMaterials.LEAF, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> LEAF_CHESTPLATE =
            ITEMS.register("leaf_chestplate", () -> new ArmorItem(ModArmorMaterials.LEAF, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> LEAF_LEGGINGS =
            ITEMS.register("leaf_leggings", () -> new ArmorItem(ModArmorMaterials.LEAF, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> LEAF_BOOTS =
            ITEMS.register("leaf_boots", () -> new ArmorItem(ModArmorMaterials.LEAF, ArmorItem.Type.BOOTS, new Item.Properties()));
}
