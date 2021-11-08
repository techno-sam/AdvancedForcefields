package com.slimeist.aforce.core.util;

import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import net.minecraft.nbt.CompoundNBT;

public class ForceNetworkPacket {

    public ForceNetworkDirection direction;

    public CompoundNBT data;

    public boolean active;

    private static final String TAG_DIRECTION = "direction";

    private static final String TAG_DATA = "data";

    private static final String TAG_ACTIVE = "active";

    public ForceNetworkPacket(ForceNetworkDirection direction, CompoundNBT data, boolean active) {
        this.direction = direction;
        this.data = data;
        this.active = active;
    }

    public ForceNetworkPacket(ForceNetworkDirection direction, CompoundNBT data) {
        this.direction = direction;
        this.data = data;
        this.active = false;
    }

    public ForceNetworkPacket(CompoundNBT nbt) {
        this.direction = ForceNetworkDirection.valueOf(nbt.getString(TAG_DIRECTION));
        this.data = nbt.getCompound(TAG_DATA);
        this.active = nbt.getBoolean(TAG_ACTIVE);
    }

    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(TAG_DIRECTION, this.direction.name());
        nbt.put(TAG_DATA, this.data);
        nbt.putBoolean(TAG_ACTIVE, this.active);
        return nbt;
    }

    public ForceNetworkPacket copy() {
        return new ForceNetworkPacket(this.direction, this.data.copy(), this.active);
    }
}
