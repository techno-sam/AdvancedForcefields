package com.slimeist.aforce.client;

import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandler {
    @SubscribeEvent
    public static void renderPlayer(final RenderPlayerEvent.Pre event) {
        PlayerEntity player = event.getPlayer();
        if (MiscUtil.isPlayerWearingFullShimmeringArmor(player)) {
            event.setCanceled(true);
        }
    }
}
