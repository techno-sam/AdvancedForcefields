package com.slimeist.aforce.client;

import com.slimeist.aforce.client.gui.ContainerScreenForceModifier;
import com.slimeist.aforce.client.render.tileentity.AlternateForceTubeTileEntityRenderer;
import com.slimeist.aforce.client.gui.ContainerScreenForceController;
import com.slimeist.aforce.core.init.ContainerTypeInit;
import com.slimeist.aforce.core.util.RenderLayerHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class StartupClient {
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event) {
        RenderLayerHandler.init();
        MenuScreens.register(ContainerTypeInit.FORCE_CONTROLLER_TYPE, ContainerScreenForceController::new);
        MenuScreens.register(ContainerTypeInit.FORCE_MODIFIER_TYPE, ContainerScreenForceModifier::new);
    }

    @SubscribeEvent
    public static void registerTERenderers(final EntityRenderersEvent.RegisterRenderers event) {
        AlternateForceTubeTileEntityRenderer.register(event);
    }

    @SubscribeEvent
    public static void loadExtraTextures(final TextureStitchEvent.Pre event) {
        if (event.getMap().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            event.addSprite(AlternateForceTubeTileEntityRenderer.SHIMMER_LOCATION);
            event.addSprite(AlternateForceTubeTileEntityRenderer.OUTLINE_LOCATION);
        }
    }
}
