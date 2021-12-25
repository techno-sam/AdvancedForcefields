package com.slimeist.aforce.common;

import com.slimeist.aforce.common.network.IMessage;
import com.slimeist.aforce.common.network.MessageForceModifierSync;
import com.slimeist.aforce.common.recipies.EnderFuelRecipe;
import com.slimeist.aforce.common.recipies.RecipeTypeEnderFuel;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import com.slimeist.aforce.core.init.*;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.slimeist.aforce.AdvancedForcefields.packetHandler;

public class StartupCommon {

    public static final IRecipeType<EnderFuelRecipe> ENDER_FUEL_RECIPE = new RecipeTypeEnderFuel();

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event)
    {
        AdvancedForcefieldsTags.init();
        registerMessage(MessageForceModifierSync.class, MessageForceModifierSync::new);
    }

    @SubscribeEvent
    public void onBlocksRegistration(final RegistryEvent.Register<Block> event) {
        BlockInit.registerAll(event);
    }

    @SubscribeEvent
    public void onItemsRegistration(final RegistryEvent.Register<Item> event) {
        ItemInit.registerAll(event);
    }

    @SubscribeEvent
    public void onTileEntitiesRegistration(final RegistryEvent.Register<TileEntityType<?>> event) {
        TileEntityTypeInit.registerAll(event);
    }

    @SubscribeEvent
    public void onContainerRegistration(final RegistryEvent.Register<ContainerType<?>> event)
    {
        ContainerTypeInit.registerAll(event);
    }

    @SubscribeEvent
    public void onRecipeRegistration(final RegistryEvent.Register<IRecipeSerializer<?>> event) {

        // Vanilla has a registry for recipe types, but it does not actively use this registry.
        // While this makes registering your recipe type an optional step, I recommend
        // registering it anyway to allow other mods to discover your custom recipe types.
        Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(ENDER_FUEL_RECIPE.toString()), ENDER_FUEL_RECIPE);

        // Register the recipe serializer. This handles from json, from packet, and to packet.
        event.getRegistry().register(EnderFuelRecipe.SERIALIZER);
    }

    @SubscribeEvent
    public void onRegistryRegistration(final RegistryEvent.NewRegistry event) {
        RegistryInit.registerAll(event);
    }

    @SubscribeEvent
    public void onModifierActionRegistration(final RegistryEvent.Register<ForceModifierRegistry> event) {
        ModifierInit.registerAll(event);
    }

    //message registering from IE

    private int messageId = 0;

    private <T extends IMessage> void registerMessage(Class<T> packetType, Function<PacketBuffer, T> decoder)
    {
        registerMessage(packetType, decoder, Optional.empty());
    }

    private <T extends IMessage> void registerMessage(
            Class<T> packetType, Function<PacketBuffer, T> decoder, NetworkDirection direction
    )
    {
        registerMessage(packetType, decoder, Optional.of(direction));
    }

    private final Set<Class<?>> knownPacketTypes = new HashSet<>();

    private <T extends IMessage> void registerMessage(
            Class<T> packetType, Function<PacketBuffer, T> decoder, Optional<NetworkDirection> direction
    )
    {
        if(!knownPacketTypes.add(packetType))
            throw new IllegalStateException("Duplicate packet type: "+packetType.getName());
        packetHandler.registerMessage(messageId++, packetType, IMessage::toBytes, decoder, (t, ctx) -> {
            t.process(ctx);
            ctx.get().setPacketHandled(true);
        }, direction);
    }

}
