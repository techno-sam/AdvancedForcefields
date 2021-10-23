package com.slimeist.aforce.common;

import com.slimeist.aforce.common.recipies.EnderFuelRecipe;
import com.slimeist.aforce.common.recipies.RecipeTypeEnderFuel;
import com.slimeist.aforce.core.init.BlockInit;
import com.slimeist.aforce.core.init.ContainerTypeInit;
import com.slimeist.aforce.core.init.ItemInit;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class StartupCommon {

    public static final IRecipeType<EnderFuelRecipe> ENDER_FUEL_RECIPE = new RecipeTypeEnderFuel();

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        AdvancedForcefieldsTags.init();
    }

    @SubscribeEvent
    public static void onBlocksRegistration(final RegistryEvent.Register<Block> event) {
        BlockInit.registerAll(event);
    }

    @SubscribeEvent
    public static void onItemsRegistration(final RegistryEvent.Register<Item> event) {
        ItemInit.registerAll(event);
    }

    @SubscribeEvent
    public static void onTileEntitiesRegistration(final RegistryEvent.Register<TileEntityType<?>> event) {
        TileEntityTypeInit.registerAll(event);
    }

    @SubscribeEvent
    public static void onContainerRegistration(final RegistryEvent.Register<ContainerType<?>> event)
    {
        ContainerTypeInit.registerAll(event);
    }

    @SubscribeEvent
    public static void onRecipeRegistration(final RegistryEvent.Register<IRecipeSerializer<?>> event) {

        // Vanilla has a registry for recipe types, but it does not actively use this registry.
        // While this makes registering your recipe type an optional step, I recommend
        // registering it anyway to allow other mods to discover your custom recipe types.
        Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(ENDER_FUEL_RECIPE.toString()), ENDER_FUEL_RECIPE);

        // Register the recipe serializer. This handles from json, from packet, and to packet.
        event.getRegistry().register(EnderFuelRecipe.SERIALIZER);
    }

}
