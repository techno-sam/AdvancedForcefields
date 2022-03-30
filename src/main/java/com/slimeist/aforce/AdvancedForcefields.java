package com.slimeist.aforce;

import com.slimeist.aforce.client.ClientProxy;
import com.slimeist.aforce.client.ClientSideOnlyModEventRegistrar;
import com.slimeist.aforce.common.CommonEventHandler;
import com.slimeist.aforce.common.CommonProxy;
import com.slimeist.aforce.common.StartupCommon;
import com.slimeist.aforce.world.OreGeneration;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.MavenVersionStringHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AdvancedForcefields.MOD_ID)
public class AdvancedForcefields {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "aforce";
    public static final String VERSION = getVersion();
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
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, OreGeneration::generateOres);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> clientSideOnlyModEventRegistrar::registerClientOnlyEvents);

        //MinecraftForge.EVENT_BUS.register(this);
    }

    private static String getVersion() {
        String versionString = "BROKEN";

        List<IModInfo> infoList = ModList.get().getModFileById(MOD_ID).getMods();
        if (infoList.stream().count()>1) {
            LOGGER.error("Multiple mods for MOD_ID: "+MOD_ID);
        }
        for (IModInfo info : infoList) {
            if (info.getModId().equals(MOD_ID)) {
                versionString = MavenVersionStringHelper.artifactVersionToString(info.getVersion());
                break;
            }
        }
        return versionString;
    }

    public static ResourceLocation getId(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}