package com.slimeist.aforce.client;

import com.slimeist.aforce.client.util.ClientUtils;
import com.slimeist.aforce.common.tiles.ForceNetworkTileEntity;
import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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

    @SubscribeEvent
    public static void renderHUD(final RenderGameOverlayEvent.Text event) {
        RayTraceResult result = ClientUtils.mc().hitResult;
        if (result!=null && result.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos pos = ((BlockRayTraceResult) result).getBlockPos();
            ClientWorld world = ClientUtils.mc().level;
            PlayerEntity player = ClientUtils.mc().player;
            if (world!=null && player!=null && MiscUtil.isPlayerWearingShimmeringHelmet(player)) {
                TileEntity tile = world.getBlockEntity(pos);
                if (tile instanceof ForceNetworkTileEntity) {
                    int distance = ((ForceNetworkTileEntity) tile).getDistance();
                    event.getLeft().add("Distance: "+distance);
                }
            }
        }
    }
}
