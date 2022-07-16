package com.slimeist.aforce.common.blocks;

import com.slimeist.aforce.core.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.util.Mth;

import java.util.Random;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

public class CustomOreBlock extends OreBlock {
    public CustomOreBlock(Properties properties, UniformInt xpDrop) {
        super(properties, xpDrop);
    }
}
