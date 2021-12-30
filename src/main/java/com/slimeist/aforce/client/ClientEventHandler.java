package com.slimeist.aforce.client;

import com.slimeist.aforce.client.util.ClientUtils;
import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandler {
    @SubscribeEvent
    public static void renderPlayer(final RenderPlayerEvent.Pre event) {
        PlayerEntity player = event.getPlayer();
        ClientPlayerEntity clientPlayer = ClientUtils.mc().player;
        if (clientPlayer!=null && !MiscUtil.isPlayerWearingShimmeringHelmet(clientPlayer) && MiscUtil.isPlayerWearingFullShimmeringArmor(player)) {
            event.setCanceled(true);
        }
    }
}
