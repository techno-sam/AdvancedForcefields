package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.AdvancedForcefields;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class ModTileEntity extends TileEntity {

    public ModTileEntity(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
    }

    public boolean isClient() {
        return this.getLevel()!=null && this.getLevel().isClientSide();
    }

    public void markDirtyFast() {
        if (level!=null) {
            level.blockEntityChanged(worldPosition, this);
            AdvancedForcefields.LOGGER.info("Marked "+this+" @ [" + worldPosition + "] as dirty");
        }
    }

    protected boolean shouldSyncOnUpdate() {
        return false;
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        return shouldSyncOnUpdate() ? new SUpdateTileEntityPacket(this.worldPosition, -1, this.getUpdateTag()) : null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }

    protected void writeSynced(CompoundNBT nbt) {}

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();
        if (nbt.contains("ForgeData", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT forgeData = nbt.getCompound("ForgeData");
            if (forgeData==this.getTileData()) {
                nbt.put("ForgeData", forgeData.copy());
            }
        }
        writeSynced(nbt);
        return nbt;
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt = super.save(nbt);
        writeSynced(nbt);
        return nbt;
    }
}
