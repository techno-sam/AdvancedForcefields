package com.slimeist.aforce.common.tiles;


import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.common.blocks.ForceTubeBlock;
import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.interfaces.IMasterLogic;
import com.slimeist.aforce.core.interfaces.IServantLogic;
import com.slimeist.aforce.core.util.ForceNetworkPacket;
import com.slimeist.aforce.core.util.NetworkBlockChain;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;

public class ForceControllerTileEntity extends ForceNetworkTileEntity {

    public ForceControllerTileEntity() {
        super(TileEntityTypeInit.FORCE_CONTROLLER_TYPE);
    }

    @Override
    public void onNetworkBuild(BlockPos masterPos) {
        super.onNetworkBuild(masterPos);
        this.distance = 0;
    }

    @Override
    public void handleDirty() {
        CompoundNBT shareddata = new CompoundNBT();
        this.writeSyncedShared(shareddata);
        CompoundNBT data = new CompoundNBT();
        data.putString(TAG_PACKET_TYPE, "DATA_SYNC");
        data.put(TAG_PACKET_MESSAGE, shareddata);

        this.addPacket(new ForceNetworkPacket(ForceNetworkDirection.TO_SERVANTS, data));
        this.markAsClean();
    }

    @Override
    public void onReceiveToServantsPacket(BlockPos myPos, int myDist, ForceNetworkPacket packet) {}

    @Override
    public void onReceiveToMasterPacket(BlockPos myPos, int myDist, ForceNetworkPacket packet) {
        if (this.getLevel()!=null && packet.data.getString(TAG_PACKET_TYPE).equals("DATA_SYNC")) {
            this.loadShared(this.getLevel().getBlockState(myPos), packet.data.getCompound(TAG_PACKET_MESSAGE).copy());
            this.markAsDirty();
        }
    }

    public boolean validTube(BlockState state, IBlockReader world, BlockPos pos) {
        boolean ok = state.is(AdvancedForcefieldsTags.Blocks.FORCE_COMPONENT_NO_CONTROLLER);
        //AdvancedForcefields.LOGGER.info("Checking if ["+state.getBlock().toString()+"] is a valid tube, and the answer is: "+ok);
        if (state.is(AdvancedForcefieldsTags.Blocks.FORCE_TUBE)) {
            ok = ok && !state.getValue(ForceTubeBlock.ENABLED);
        }
        return ok; //allow network to be built through modifiers etc. too
    }

    public boolean validComponent(BlockState state, IBlockReader world, BlockPos pos) {
        boolean ok = state.is(AdvancedForcefieldsTags.Blocks.FORCE_COMPONENT_NO_CONTROLLER);
        //AdvancedForcefields.LOGGER.info("Checking if ["+state.getBlock().toString()+"] is a valid component, and the answer is: "+ok);
        if (state.is(AdvancedForcefieldsTags.Blocks.FORCE_TUBE)) {
            ok = ok && !state.getValue(ForceTubeBlock.ENABLED);
        }
        return ok;
    }

    //time to start building our network
    public void onPowered() {
        NetworkBlockChain blockChain = new NetworkBlockChain(this.getLevel(), this.getBlockPos(),
                128, this::validTube, this::validComponent).create();
        ArrayList<BlockPos> blocks = blockChain.getComponentBlocks();
        HashMap<BlockPos, Integer> distances = blockChain.getDistances();
        //AdvancedForcefields.LOGGER.info("ForceController powered with a blocklist of length: "+blocks.size());
        this.onNetworkBuild(this.getBlockPos());
        for (BlockPos pos : blocks) {
            if (this.getLevel()!=null) {
                TileEntity tile = this.getLevel().getBlockEntity(pos);
                if (tile instanceof ForceNetworkTileEntity) {
                    ForceNetworkTileEntity networkTile = (ForceNetworkTileEntity) tile;
                    networkTile.onNetworkBuild(this.getBlockPos());
                    networkTile.setDistance(distances.get(pos));
                    networkTile.setLocked(true);
                    World world = this.getLevel();
                    BlockState state = world.getBlockState(pos);
                    if (state.is(AdvancedForcefieldsTags.Blocks.FORCE_TUBE)) {
                        world.setBlock(pos, state.setValue(ForceTubeBlock.ENABLED, true), Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.NO_RERENDER);
                    }
                }
            }
        }
    }

    public void onDepowered() {
        CompoundNBT data = new CompoundNBT();
        data.putString(TAG_PACKET_TYPE, "NETWORK_RELEASE");
        data.put(TAG_PACKET_MESSAGE, new CompoundNBT());

        this.addPacket(new ForceNetworkPacket(ForceNetworkDirection.TO_SERVANTS, data, true));
        this.handlePackets();
        this.onNetworkBuild(null);
        this.setDistance(-1);
    }
}