package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.items.ArmorMaterials;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class ItemInit {
    //public static BlockItem BASE_PIPE;
    public static BlockItem FORCE_TUBE;
    public static BlockItem FORCE_CONTROLLER;
    public static BlockItem FORCE_MODIFIER;

    public static ArmorItem SHIMMERING_HELMET;
    public static ArmorItem SHIMMERING_CHESTPLATE;
    public static ArmorItem SHIMMERING_LEGGINGS;
    public static ArmorItem SHIMMERING_BOOTS;

    private ItemInit() {}

    public static void registerAll(RegistryEvent.Register<Item> event) {

        //BlockItems
        FORCE_TUBE = register("force_tube", new BlockItem(BlockInit.FORCE_TUBE, new Item.Properties()
                .tab(ItemGroup.TAB_MISC)
        ));
        FORCE_CONTROLLER = register("force_controller", new BlockItem(BlockInit.FORCE_CONTROLLER, new Item.Properties()
                .tab(ItemGroup.TAB_MISC)
        ));
        FORCE_MODIFIER = register("force_modifier", new BlockItem(BlockInit.FORCE_MODIFIER, new Item.Properties()
                .tab(ItemGroup.TAB_MISC)
        ));

        //Shimmering Armor
        SHIMMERING_HELMET = register("shimmering_helmet", new ArmorItem(ArmorMaterials.SHIMMERING_GOLD, EquipmentSlotType.HEAD,
                (new Item.Properties().tab(ItemGroup.TAB_COMBAT))));
        SHIMMERING_CHESTPLATE = register("shimmering_chestplate", new ArmorItem(ArmorMaterials.SHIMMERING_GOLD, EquipmentSlotType.CHEST,
                (new Item.Properties().tab(ItemGroup.TAB_COMBAT))));
        SHIMMERING_LEGGINGS = register("shimmering_leggings", new ArmorItem(ArmorMaterials.SHIMMERING_GOLD, EquipmentSlotType.LEGS,
                (new Item.Properties().tab(ItemGroup.TAB_COMBAT))));
        SHIMMERING_BOOTS = register("shimmering_boots", new ArmorItem(ArmorMaterials.SHIMMERING_GOLD, EquipmentSlotType.FEET,
                (new Item.Properties().tab(ItemGroup.TAB_COMBAT))));
    }

    private static <T extends Item> T register(String name, T item) {
        ResourceLocation id = AdvancedForcefields.getId(name);
        item.setRegistryName(id);
        ForgeRegistries.ITEMS.register(item);
        return item;
    }
}
