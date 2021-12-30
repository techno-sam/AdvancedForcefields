package com.slimeist.aforce.core.util;

import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
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

    public static boolean isPlayerWearingFullShimmeringArmor(PlayerEntity player) {
        return player.getItemBySlot(EquipmentSlotType.HEAD).getItem().is(AdvancedForcefieldsTags.Items.SHIMMERING_HELMET)&&
                player.getItemBySlot(EquipmentSlotType.CHEST).getItem().is(AdvancedForcefieldsTags.Items.SHIMMERING_CHESTPLATE)&&
                player.getItemBySlot(EquipmentSlotType.LEGS).getItem().is(AdvancedForcefieldsTags.Items.SHIMMERING_LEGGINGS)&&
                player.getItemBySlot(EquipmentSlotType.FEET).getItem().is(AdvancedForcefieldsTags.Items.SHIMMERING_BOOTS);
    }
}
