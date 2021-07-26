package com.slimeist.aforce.common.tiles;


import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.interfaces.IMasterLogic;
import com.slimeist.aforce.core.interfaces.IServantLogic;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public class ForceControllerTileEntity extends ModTileEntity implements IMasterLogic {

    private static final String TAG_BLOCKING_MODE = "blockingMode"; //can be 1:'NONE', 2:'ALL', 3:'PLAYERS', 4:'MOBS'

    private int blockingMode = 1;

    public int getBlockingMode() {
        return this.blockingMode;
    }

    public void setBlockingMode(int blockingMode) {
        this.blockingMode = blockingMode;
    }

    public ForceControllerTileEntity() {
        super(TileEntityTypeInit.FORCE_CONTROLLER_TYPE);
    }

    @Override
    public void notifyChange(IServantLogic servant, BlockPos pos, BlockState state) {
        AdvancedForcefields.LOGGER.info("Notifying change at pos: "+pos.toString());
    }

    @Override
    public void load(BlockState blockState, CompoundNBT nbt) {
        super.load(blockState, nbt);
        this.setBlockingMode(nbt.getInt(TAG_BLOCKING_MODE));
    }

    @Override
    public void writeSynced(CompoundNBT nbt) {
        super.writeSynced(nbt);
        nbt.putInt(TAG_BLOCKING_MODE, this.getBlockingMode());
    }

    @Override
    public boolean shouldSyncOnUpdate() {
        return true;
    }
}
