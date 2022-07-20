package com.slimeist.aforce.common.blocks;

import com.slimeist.aforce.common.tiles.AdvancedForceModifierTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class AdvancedForceModifierBlock extends ForceModifierBlock{
    public AdvancedForceModifierBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader world) {
        return new AdvancedForceModifierTileEntity();
    }
}
