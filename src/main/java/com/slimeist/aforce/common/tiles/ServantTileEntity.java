package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.core.interfaces.IMasterLogic;
import com.slimeist.aforce.core.interfaces.IServantLogic;
import com.slimeist.aforce.core.util.TagUtil;
import com.slimeist.aforce.core.util.TileEntityHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;

public class ServantTileEntity extends ModTileEntity implements IServantLogic {
    private static final String TAG_MASTER_POS = "masterPos";
    private static final String TAG_MASTER_BLOCK = "masterBlock";

    @Nullable
    private BlockPos masterPos;
    @Nullable
    private Block masterBlock;
    public ServantTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public Block getMasterBlock() {
        return masterBlock;
    }

    /** Checks if this servant has a master */
    public boolean hasMaster() {
        validateMaster();
        return masterPos != null;
    }

    /**
     * Called to change the master
     * @param master  New master
     * @param block   New master block
     */
    protected void setMaster(@Nullable BlockPos master, @Nullable Block block) {
        masterPos = master;
        masterBlock = block;
        this.markDirtyFast();
    }

    /**
     * Checks that this servant has a valid master. Clears the master if invalid
     * @return  True if this servant has a valid master
     */
    protected boolean validateMaster() {
        if (masterPos == null) {
            return false;
        }

        // ensure the master block is correct
        assert level != null;
        if (level.getBlockState(masterPos).getBlock() == masterBlock) {
            return true;
        }
        // master invalid, so clear
        setMaster(null, null);
        return false;
    }

    @Override
    public boolean isValidMaster(IMasterLogic master) {
        // if we have a valid master, the passed master is only valid if its our current master
        if (validateMaster()) {
            return master.getTileEntity().getBlockPos().equals(this.masterPos);
        }
        // otherwise, we are happy with any master
        return true;
    }

    @Override
    public void notifyMasterOfChange(BlockPos pos, BlockState state) {
        if (validateMaster()) {
            assert masterPos != null;
            TileEntityHelper.getTile(IMasterLogic.class, level, masterPos).ifPresent(te -> te.notifyChange(this, pos, state));
        }
    }

    @Override
    public void setPotentialMaster(IMasterLogic master) {
        TileEntity masterTE = master.getTileEntity();
        BlockPos newMaster = masterTE.getBlockPos();
        // if this is our current master, simply update the master block
        if (newMaster.equals(this.masterPos)) {
            masterBlock = masterTE.getBlockState().getBlock();
            this.markDirtyFast();
            // otherwise, only set if we don't have a master
        } else if (!validateMaster()) {
            setMaster(newMaster, masterTE.getBlockState().getBlock());
        }
    }

    @Override
    public void removeMaster(IMasterLogic master) {
        if (masterPos != null && masterPos.equals(master.getTileEntity().getBlockPos())) {
            setMaster(null, null);
        }
    }


    /* NBT */

    /**
     * Reads the master from NBT
     * @param tags  NBT to read
     */
    protected void readMaster(CompoundNBT tags) {
        BlockPos masterPos = TagUtil.readPos(tags, TAG_MASTER_POS);
        Block masterBlock = null;
        // if the master position is valid, get the master block
        if (masterPos != null && tags.contains(TAG_MASTER_BLOCK, Constants.NBT.TAG_STRING)) {
            ResourceLocation masterBlockName = ResourceLocation.tryParse(tags.getString(TAG_MASTER_BLOCK));
            if (masterBlockName != null && ForgeRegistries.BLOCKS.containsKey(masterBlockName)) {
                masterBlock = ForgeRegistries.BLOCKS.getValue(masterBlockName);
            }
        }
        // if both valid, set
        if (masterBlock != null) {
            this.masterPos = masterPos;
            this.masterBlock = masterBlock;
        }
    }

    @Override
    public void load(BlockState blockState, CompoundNBT tags) {
        super.load(blockState, tags);
        readMaster(tags);
    }

    /**
     * Writes the master position and master block to the given compound
     * @param tags  Tags
     */
    protected CompoundNBT writeMaster(CompoundNBT tags) {
        if (masterPos != null && masterBlock != null) {
            tags.put(TAG_MASTER_POS, TagUtil.writePos(masterPos));
            tags.putString(TAG_MASTER_BLOCK, Objects.requireNonNull(masterBlock.getRegistryName()).toString());
        }
        return tags;
    }

    @Override
    public CompoundNBT save(CompoundNBT tags) {
        tags = super.save(tags);
        writeMaster(tags);
        return tags;
    }
}
