package com.slimeist.aforce.common.blocks;

import com.slimeist.aforce.common.tiles.ForceControllerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class ForceControllerBlock extends Block {

    public ForceControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState block, IBlockReader world) {
        return new ForceControllerTileEntity();
    }
}
