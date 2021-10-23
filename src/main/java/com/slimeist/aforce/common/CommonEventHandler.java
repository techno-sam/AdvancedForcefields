package com.slimeist.aforce.common;

import com.slimeist.aforce.common.blocks.ForceControllerBlock;
import com.slimeist.aforce.core.init.BlockInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonEventHandler {
    @SubscribeEvent
    public static void onBlockBreak(final BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        Block block = state.getBlock();
        IWorld world = event.getWorld();
        if (block.is(BlockInit.FORCE_CONTROLLER)) {
            if (!((ForceControllerBlock) block).canEntityDestroy(state, world, event.getPos(), event.getPlayer())) {
                event.setCanceled(true);
            }
        }
    }
}
