package com.slimeist.aforce.core.util;

import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;

public class MiscUtil {

    public static void syncTE(TileEntity tile) {
        SUpdateTileEntityPacket packet = tile.getUpdatePacket();
        if (packet!=null && tile.getLevel() instanceof ServerWorld) {
            ((ServerChunkProvider) tile.getLevel().getChunkSource()).chunkMap
                    .getPlayers(new ChunkPos(tile.getBlockPos()), false)
                    .forEach(e -> e.connection.send(packet));
        }
    }
}
