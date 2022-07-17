package com.slimeist.aforce.common;

import com.slimeist.aforce.common.network.IMessage;
import com.slimeist.aforce.common.network.MessageForceModifierSync;
import com.slimeist.aforce.common.recipies.EnderFuelRecipe;
import com.slimeist.aforce.common.recipies.RecipeTypeEnderFuel;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import com.slimeist.aforce.core.init.*;
import com.slimeist.aforce.world.OreGeneration;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fmllegacy.network.NetworkDirection;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.slimeist.aforce.AdvancedForcefields.packetHandler;

public class StartupCommon {

    public static final RecipeType<EnderFuelRecipe> ENDER_FUEL_RECIPE = new RecipeTypeEnderFuel();

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event)
    {
        AdvancedForcefieldsTags.init();
        registerMessage(MessageForceModifierSync.class, MessageForceModifierSync::new);
        event.enqueueWork(OreGeneration::registerOres);
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
    public void onTileEntitiesRegistration(final RegistryEvent.Register<BlockEntityType<?>> event) {
        TileEntityTypeInit.registerAll(event);
    }

    @SubscribeEvent
    public void onContainerRegistration(final RegistryEvent.Register<MenuType<?>> event)
    {
        ContainerTypeInit.registerAll(event);
    }

    @SubscribeEvent
    public void onRecipeRegistration(final RegistryEvent.Register<RecipeSerializer<?>> event) {

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

    private <T extends IMessage> void registerMessage(Class<T> packetType, Function<FriendlyByteBuf, T> decoder)
    {
        registerMessage(packetType, decoder, Optional.empty());
    }

    private <T extends IMessage> void registerMessage(
            Class<T> packetType, Function<FriendlyByteBuf, T> decoder, NetworkDirection direction
    )
    {
        registerMessage(packetType, decoder, Optional.of(direction));
    }

    private final Set<Class<?>> knownPacketTypes = new HashSet<>();

    private <T extends IMessage> void registerMessage(
            Class<T> packetType, Function<FriendlyByteBuf, T> decoder, Optional<NetworkDirection> direction
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
