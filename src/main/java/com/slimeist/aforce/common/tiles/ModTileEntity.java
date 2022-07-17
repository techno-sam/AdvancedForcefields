package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ModTileEntity extends BlockEntity {
    public ModTileEntity(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state) {
        super(tileEntityType, pos, state);
    }

    public boolean isClient() {
        return this.getLevel()!=null && this.getLevel().isClientSide();
    }

    public void markDirtyFast() {
        if (level!=null) {
            level.blockEntityChanged(worldPosition);
            //AdvancedForcefields.LOGGER.info("Marked "+this+" @ [" + worldPosition + "] as dirty");
            if (shouldSyncOnUpdate()) {
                MiscUtil.syncTE(this);
            }
        }
    }

    protected boolean shouldSyncOnUpdate() {
        return false;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return shouldSyncOnUpdate() ? ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag) : null;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    protected void writeSynced(CompoundTag nbt) {}

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        if (nbt.contains("ForgeData", Tag.TAG_COMPOUND)) {
            CompoundTag forgeData = nbt.getCompound("ForgeData");
            if (forgeData==this.getTileData()) {
                nbt.put("ForgeData", forgeData.copy());
            }
        }
        writeSynced(nbt);
        return nbt;
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        writeSynced(nbt);
    }
}
