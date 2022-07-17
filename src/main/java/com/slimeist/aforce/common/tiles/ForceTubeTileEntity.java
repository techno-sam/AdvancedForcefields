package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.init.BlockInit;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.util.ForceNetworkPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class ForceTubeTileEntity extends ForceNetworkTileEntity {

    protected boolean shouldSignal = true;

    protected long lastToMasterPacketGT = 0; //we don't need a toServant variable, because FNTE does that already

    protected ArrayList<BlockPos> closerNodes = new ArrayList<>();
    protected ArrayList<BlockPos> fartherNodes = new ArrayList<>();
    protected boolean nodesNeedUpdating = false;
    protected long lastUpdate = 0;

    public ForceTubeTileEntity(BlockPos pos, BlockState state) {
        super(TileEntityTypeInit.FORCE_TUBE_TYPE, pos, state);
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

    public static <T extends ForceTubeTileEntity> void tick(Level level, BlockPos pos, BlockState state, T tile) {
        ForceNetworkTileEntity.networkTick(level, pos, state, tile);
        if (tile.getLevel() != null && tile.getLevel().getGameTime()>tile.lastUpdate+60) {
            tile.nodesNeedUpdating = true;
            tile.lastUpdate = tile.getLevel().getGameTime();
        }
        if (tile.nodesNeedUpdating) {
            tile.updateNodeLists();
        }
    }

    protected void updateNodeLists() {
        this.closerNodes.clear();
        this.fartherNodes.clear();
        if (this.getLevel() != null && this.getLevel().isClientSide()) {
            BlockPos myPos = this.getBlockPos();
            for (Direction dir : Direction.values()) {
                if (this.isConnected(dir)) {
                    BlockEntity te = this.getLevel().getBlockEntity(myPos.relative(dir));
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
        CompoundTag data = new CompoundTag();
        data.putString(TAG_PACKET_TYPE, "NETWORK_RELEASE");
        data.put(TAG_PACKET_MESSAGE, new CompoundTag());
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
