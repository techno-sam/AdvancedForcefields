package com.slimeist.aforce.core.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeTileEntity;

public interface IMasterLogic extends IForgeTileEntity {
    //Copied from Tinker's Construct
    /**
     * Called when servants change their state
     *
     * @param servant  Servant tile instance
     * @param pos      Position that changed. May not be the servant position
     * @param state    State that changed. May not be the servant state
     */
    void notifyChange(IServantLogic servant, BlockPos pos, BlockState state);
}
