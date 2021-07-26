package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.common.blocks.ForceTubeBlock;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.interfaces.IMasterLogic;
import com.slimeist.aforce.core.interfaces.IServantLogic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;

public class ForceTubeTileEntity extends ServantTileEntity implements ITickableTileEntity {

    private static final String ACTIVE_TAG = "ForceActive";

    public ForceTubeTileEntity() {
        super(TileEntityTypeInit.FORCE_TUBE_TYPE);
    }

    private boolean isActive = false;
    private boolean skipSync = false;

    public boolean isActive() {
        return this.isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void activate() {
        this.setActive(true);
    }

    public void deactivate() {
        this.setActive(false);
    }

    @Override
    public void tick() {
        BlockPos pos = this.getBlockPos();
        if(level.getGameTime() % 10 == 0 && level.isClientSide()) {
            float r = 1.0F;
            float g = 0.5F;
            float b = 0.1F;
            float scale = 2.5F;
            float offset = 0.5F;
            level.addParticle(new RedstoneParticleData(r, g, b, scale), pos.getX()+offset, pos.getY()+offset, pos.getZ()+offset, 0.0D, 0.0D, 0.0D);
        }
        if (!level.isClientSide()) {
            BlockState state = this.getBlockState();
            if (level.getGameTime()%10==0) {
                if (state.getValue(ForceTubeBlock.ENABLED) != this.hasMaster()) {
                    //AdvancedForcefields.LOGGER.info("ENABLED value and master state do not match");
                    state = state.setValue(ForceTubeBlock.ENABLED, this.hasMaster());
                    level.setBlockAndUpdate(pos, state);
                }
            }
        }
    }

    public int getComparatorOutput() {
        return this.isActive() ? 15 : 0;
    }

    @Override
    public void load(BlockState blockState, CompoundNBT nbt) {
        super.load(blockState, nbt);
        this.setActive(nbt.getBoolean(ACTIVE_TAG));
    }

    @Override
    public void writeSynced(CompoundNBT nbt) {
        super.writeSynced(nbt);
        nbt.putBoolean(ACTIVE_TAG, this.isActive());
    }

    @Override
    public boolean shouldSyncOnUpdate() {
        return true;
    }

    /**
     * Block method to update neighbors of a smeltery component when a new one is placed
     * @param world  World instance
     * @param pos    Location of new smeltery component
     */
    public static void updateNeighbors(World world, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            // if the neighbor is a master, notify it we exist
            TileEntity tileEntity = world.getBlockEntity(pos.offset(direction.getNormal()));
            if (tileEntity instanceof IMasterLogic && tileEntity instanceof ForceControllerTileEntity) {
                AdvancedForcefields.LOGGER.info("Neighbor is a ForceControllerTileEntity");
                TileEntity servant = world.getBlockEntity(pos);
                if (servant instanceof IServantLogic) {
                    AdvancedForcefields.LOGGER.info("Found a servant");
                    ((IServantLogic) servant).setPotentialMaster((IMasterLogic) tileEntity);
                    ((IMasterLogic) tileEntity).notifyChange((IServantLogic) servant, pos, state);
                    break;
                }
                // if the neighbor is a servant, notify its master we exist
            } else if (tileEntity instanceof ForceTubeTileEntity) {
                AdvancedForcefields.LOGGER.info("Neighbor is a ForceTubeTileEntity");
                ForceTubeTileEntity tubeTileEntity = (ForceTubeTileEntity) tileEntity;
                if (tubeTileEntity.hasMaster()) {
                    AdvancedForcefields.LOGGER.info("Neighbor has a master");

                    TileEntity me = world.getBlockEntity(pos);
                    if (me instanceof IServantLogic) {
                        //((IServantLogic) me).setPotentialMaster((IMasterLogic) world.getBlockEntity(tubeTileEntity.getMasterPos()));
                        BlockPos masterPos = tubeTileEntity.getMasterPos();
                        if (masterPos!=null) {
                            ForceControllerTileEntity masterEntity = (ForceControllerTileEntity) world.getBlockEntity(masterPos);
                            ((IServantLogic) me).setPotentialMaster(masterEntity);
                        }
                    }

                    tubeTileEntity.notifyMasterOfChange(pos, state);
                    break;
                }
            }
        }
    }
}
