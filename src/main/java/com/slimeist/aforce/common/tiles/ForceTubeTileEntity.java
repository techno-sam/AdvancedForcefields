package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.util.ForceNetworkPacket;
import net.minecraft.nbt.CompoundNBT;

public class ForceTubeTileEntity extends ForceNetworkTileEntity {

    protected boolean shouldSignal = true;

    public ForceTubeTileEntity() {
        super(TileEntityTypeInit.FORCE_TUBE_TYPE);
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

    public void networkDisconnect() {
        CompoundNBT data = new CompoundNBT();
        data.putString(TAG_PACKET_TYPE, "NETWORK_RELEASE");
        data.put(TAG_PACKET_MESSAGE, new CompoundNBT());
        ForceNetworkPacket release_packet = new ForceNetworkPacket(ForceNetworkDirection.TO_SERVANTS, data, true);
        this.onReceiveToServantsPacket(this.getBlockPos(), this.getDistance(), release_packet);
    }
}
