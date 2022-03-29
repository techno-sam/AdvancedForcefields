package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.init.BlockInit;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.util.ForceNetworkPacket;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class ForceTubeTileEntity extends ForceNetworkTileEntity {

    protected boolean shouldSignal = true;

    protected long lastToMasterPacketGT = 0; //we don't need a toServant variable, because FNTE does that already

    protected ArrayList<BlockPos> closerNodes = new ArrayList<>();
    protected ArrayList<BlockPos> fartherNodes = new ArrayList<>();
    protected boolean nodesNeedUpdating = false;
    protected long lastUpdate = 0;

    public ForceTubeTileEntity() {
        super(TileEntityTypeInit.FORCE_TUBE_TYPE);
        this.updateNodeLists();
    }

    public int getComparatorOutput() {
        return this.isLocked() ? 15 : 0;
    }

    public boolean isSignalling() {
        return this.shouldSignal;
    }

    public void setSignalling(boolean shouldSignal) {
        this.shouldSignal = shouldSignal;
    }

    @Override
    public void setDistance(int distance) {
        super.setDistance(distance);
        this.nodesNeedUpdating = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getLevel()!=null && this.getLevel().getGameTime()>this.lastUpdate+60) {
            this.nodesNeedUpdating = true;
            this.lastUpdate = this.getLevel().getGameTime();
        }
        if (this.nodesNeedUpdating) {
            this.updateNodeLists();
        }
    }

    protected void updateNodeLists() {
        this.closerNodes.clear();
        this.fartherNodes.clear();
        if (this.getLevel() != null && this.getLevel().isClientSide()) {
            BlockPos myPos = this.getBlockPos();
            for (Direction dir : Direction.values()) {
                if (this.isConnected(dir)) {
                    TileEntity te = this.getLevel().getBlockEntity(myPos.relative(dir));
                    if (te instanceof ForceNetworkTileEntity) {
                        ForceNetworkTileEntity otherFNTE = (ForceNetworkTileEntity) te;
                        if (this.sharesMasterPos(myPos.relative(dir))) {
                            if (otherFNTE.getDistance() < this.getDistance()) {
                                this.closerNodes.add(myPos.relative(dir));
                            } else if (otherFNTE.getDistance() > this.getDistance()) {
                                this.fartherNodes.add(myPos.relative(dir));
                            }
                        }
                    }
                }
            }
        }
        this.nodesNeedUpdating = false;
    }

    public long getLastToMasterPacketGT() {
        return lastToMasterPacketGT;
    }

    public ArrayList<BlockPos> getCloserNodes() {
        return closerNodes;
    }

    public ArrayList<BlockPos> getFartherNodes() {
        return fartherNodes;
    }

    public ArrayList<BlockPos> getConnectedNodes() {
        ArrayList<BlockPos> connected = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            BlockPos otherPos = this.getBlockPos().relative(dir);
            if (this.isConnected(otherPos)) {
                connected.add(otherPos);
            }
        }
        return connected;
    }

    @Override
    public void onReceiveToMasterPacket(BlockPos myPos, int myDist, ForceNetworkPacket packet) {
        if (this.getLevel()!=null) {
            this.lastToMasterPacketGT = this.getLevel().getGameTime();
            this.nodesNeedUpdating = true;
        }
        super.onReceiveToMasterPacket(myPos, myDist, packet);
    }

    public void networkDisconnect() {
        CompoundNBT data = new CompoundNBT();
        data.putString(TAG_PACKET_TYPE, "NETWORK_RELEASE");
        data.put(TAG_PACKET_MESSAGE, new CompoundNBT());
        ForceNetworkPacket release_packet = new ForceNetworkPacket(ForceNetworkDirection.TO_SERVANTS, data, this.getBlockPos(), true);
        this.onReceiveToServantsPacket(this.getBlockPos(), this.getDistance(), release_packet);
        this.updateBlockState();
    }

    @Override
    public void postNetworkBuild() {
        super.postNetworkBuild();
        this.nodesNeedUpdating = true;
        this.updateBlockState();
    }

    protected void updateBlockState() {
        if (this.getLevel() != null) {
            if (this.getLevel().getBlockState(this.getBlockPos()).is(AdvancedForcefieldsTags.Blocks.FORCE_TUBE)) {
                BlockInit.FORCE_TUBE.doUpdate(this.getLevel().getBlockState(this.getBlockPos()), this.getLevel(), this.getBlockPos());
            }
        }
    }
}
