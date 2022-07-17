package com.slimeist.aforce.core.util;

import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class ForceNetworkPacket {

    public ForceNetworkDirection direction;

    public CompoundTag data;

    public boolean active;

    public long createdGameTime;

    public BlockPos originPos; //the position of the TE which passed this on

    private static final String TAG_DIRECTION = "direction";

    private static final String TAG_DATA = "data";

    private static final String TAG_ACTIVE = "active";

    private static final String TAG_GAME_TIME = "createdGameTime";

    private static final String TAG_ORIGIN_POS = "originPos";

    public ForceNetworkPacket(ForceNetworkDirection direction, CompoundTag data, BlockPos originPos, boolean active) {
        this.direction = direction;
        this.data = data;
        this.active = active;
        this.createdGameTime = -1;
        this.originPos = originPos;
    }

    public ForceNetworkPacket(ForceNetworkDirection direction, CompoundTag data, BlockPos originPos) {
        this(direction, data, originPos,false);
    }

    public ForceNetworkPacket(CompoundTag nbt) {
        this.direction = ForceNetworkDirection.valueOf(nbt.getString(TAG_DIRECTION));
        this.data = nbt.getCompound(TAG_DATA);
        this.active = nbt.getBoolean(TAG_ACTIVE);
        if (nbt.contains(TAG_GAME_TIME, Tag.TAG_LONG)) {
            this.createdGameTime = nbt.getLong(TAG_GAME_TIME);
        } else {
            this.createdGameTime = -1;
        }
        this.originPos = TagUtil.readPos(nbt.getCompound(TAG_ORIGIN_POS));
    }

    public ForceNetworkPacket setCreatedGameTime(long gameTime) {
        this.createdGameTime = gameTime;
        return this;
    }

    public ForceNetworkPacket setOriginPos(BlockPos originPos) {
        this.originPos = originPos;
        return this;
    }

    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(TAG_DIRECTION, this.direction.name());
        nbt.put(TAG_DATA, this.data);
        nbt.putBoolean(TAG_ACTIVE, this.active);
        nbt.putLong(TAG_GAME_TIME, this.createdGameTime);
        if (this.originPos == null) {
            this.originPos = new BlockPos(0, 0, 0);
        }
        nbt.put(TAG_ORIGIN_POS, TagUtil.writePos(this.originPos));
        return nbt;
    }

    public ForceNetworkPacket copy() {
        return new ForceNetworkPacket(this.direction, this.data.copy(), this.originPos, this.active).setCreatedGameTime(this.createdGameTime);
    }
}
