package com.slimeist.aforce.core.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IForceNetworkBlock {
    int getDistance(Level world, BlockPos pos);
    void setDistance(Level world, BlockPos pos, int distance);
    boolean hasCloser(Level world, BlockPos pos, BlockPos bannedPos, int distance);
}
