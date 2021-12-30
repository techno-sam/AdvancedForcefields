package com.slimeist.aforce.client;

import com.slimeist.aforce.client.gui.ContainerScreenForceModifier;
import com.slimeist.aforce.client.render.tileentity.AlternateForceTubeTileEntityRenderer;
import com.slimeist.aforce.client.gui.ContainerScreenForceController;
import com.slimeist.aforce.core.init.ContainerTypeInit;
import com.slimeist.aforce.core.util.RenderLayerHandler;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class StartupClient {
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event) {
        RenderLayerHandler.init();
        AlternateForceTubeTileEntityRenderer.register();
        ScreenManager.register(ContainerTypeInit.FORCE_CONTROLLER_TYPE, ContainerScreenForceController::new);
        ScreenManager.register(ContainerTypeInit.FORCE_MODIFIER_TYPE, ContainerScreenForceModifier::new);
    }

    @SubscribeEvent
    public static void loadExtraTextures(final TextureStitchEvent.Pre event) {
        if (event.getMap().location().equals(PlayerContainer.BLOCK_ATLAS)) {
            event.addSprite(AlternateForceTubeTileEntityRenderer.SHIMMER_LOCATION);
            event.addSprite(AlternateForceTubeTileEntityRenderer.OUTLINE_LOCATION);
        }
    }
}
