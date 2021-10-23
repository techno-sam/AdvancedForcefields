package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Foods;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class ItemInit {
    //public static BlockItem BASE_PIPE;
    public static BlockItem FORCE_TUBE;
    public static BlockItem FORCE_CONTROLLER;
    public static BlockItem FORCE_MODIFIER;

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

        /*BASE_PIPE = register("base_pipe", new BlockItem(BlockInit.BASE_PIPE, new Item.Properties()
                .tab(ItemGroup.TAB_MISC)
        ));*/
        //initializeSpawnEggs();
    }

    private static <T extends Item> T register(String name, T item) {
        ResourceLocation id = AdvancedForcefields.getId(name);
        item.setRegistryName(id);
        ForgeRegistries.ITEMS.register(item);
        return item;
    }

    //public static void initializeSpawnEggs() {
    //    ModSpawnEggItem.initUnaddedEggs();
    //}
}
