package com.slimeist.aforce;

import com.slimeist.aforce.client.ClientProxy;
import com.slimeist.aforce.client.ClientSideOnlyModEventRegistrar;
import com.slimeist.aforce.client.render.tileentity.ForceTubeTileEntityRenderer;
import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.common.CommonEventHandler;
import com.slimeist.aforce.common.CommonProxy;
import com.slimeist.aforce.common.StartupCommon;
import com.slimeist.aforce.core.init.BlockInit;
import com.slimeist.aforce.core.init.ItemInit;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.util.RenderLayerHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AdvancedForcefields.MOD_ID)
public class AdvancedForcefields {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "aforce";
    public static final String VERSION = "0.5";//""${version}";
    public static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

    public static final SimpleChannel packetHandler = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MOD_ID, "main"))
            .networkProtocolVersion(() -> VERSION)
            .serverAcceptedVersions(VERSION::equals)
            .clientAcceptedVersions(VERSION::equals)
            .simpleChannel();

    public AdvancedForcefields() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        final ClientSideOnlyModEventRegistrar clientSideOnlyModEventRegistrar = new ClientSideOnlyModEventRegistrar(bus);

        bus.register(new StartupCommon());
        MinecraftForge.EVENT_BUS.register(CommonEventHandler.class);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> clientSideOnlyModEventRegistrar::registerClientOnlyEvents);

        //MinecraftForge.EVENT_BUS.register(this);
    }



    public static ResourceLocation getId(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}