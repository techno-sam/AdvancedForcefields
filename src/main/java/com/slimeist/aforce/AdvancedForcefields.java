package com.slimeist.aforce;

import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.core.init.BlockInit;
import com.slimeist.aforce.core.init.ItemInit;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.util.RenderLayerHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AdvancedForcefields.MOD_ID)
public class AdvancedForcefields {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "aforce";

    public AdvancedForcefields() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        bus.addListener(this::setupClient);

        bus.addGenericListener(Item.class, ItemInit::registerAll);
        bus.addGenericListener(Block.class, BlockInit::registerAll);
        bus.addGenericListener(TileEntityType.class, TileEntityTypeInit::registerAll);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        AdvancedForcefieldsTags.init();
    }

    private void setupClient(final FMLClientSetupEvent event) {
        RenderLayerHandler.init();
    }

    public static ResourceLocation getId(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}