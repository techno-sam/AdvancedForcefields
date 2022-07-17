package com.slimeist.aforce.client;

import com.slimeist.aforce.client.util.ClientUtils;
import com.slimeist.aforce.common.tiles.ForceNetworkTileEntity;
import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandler {
    @SubscribeEvent
    public static void renderPlayer(final RenderPlayerEvent.Pre event) {
        Player player = event.getPlayer();
        LocalPlayer clientPlayer = ClientUtils.mc().player;
        if (clientPlayer!=null && !MiscUtil.isPlayerWearingShimmeringHelmet(clientPlayer) && MiscUtil.isPlayerWearingFullShimmeringArmor(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void renderHUD(final RenderGameOverlayEvent.Text event) {
        HitResult result = ClientUtils.mc().hitResult;
        if (result!=null && result.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) result).getBlockPos();
            ClientLevel world = ClientUtils.mc().level;
            Player player = ClientUtils.mc().player;
            if (world!=null && player!=null && MiscUtil.isPlayerWearingShimmeringHelmet(player)) {
                BlockEntity tile = world.getBlockEntity(pos);
                if (tile instanceof ForceNetworkTileEntity) {
                    int distance = ((ForceNetworkTileEntity) tile).getDistance();
                    event.getLeft().add("Distance: "+distance);
                }
            }
        }
    }
}
