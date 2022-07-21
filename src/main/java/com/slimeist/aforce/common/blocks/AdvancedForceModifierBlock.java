package com.slimeist.aforce.common.blocks;

import com.slimeist.aforce.common.tiles.AdvancedForceModifierTileEntity;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class AdvancedForceModifierBlock extends ForceModifierBlock {
    public AdvancedForceModifierBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedForceModifierTileEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_) {
        return createTickerHelper(p_153214_, TileEntityTypeInit.ADVANCED_FORCE_MODIFIER_TYPE, AdvancedForceModifierTileEntity::tick);
    }
}
