package com.slimeist.aforce.core.interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IForceNetworkBlock {
    int getDistance(World world, BlockPos pos);
    void setDistance(World world, BlockPos pos, int distance);
    boolean hasCloser(World world, BlockPos pos, BlockPos bannedPos, int distance);
}
