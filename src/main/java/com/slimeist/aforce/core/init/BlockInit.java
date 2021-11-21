package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.blocks.ForceControllerBlock;
import com.slimeist.aforce.common.blocks.ForceModifierBlock;
import com.slimeist.aforce.common.blocks.ForceTubeBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.ToIntFunction;

public final class BlockInit {
    //public static BasePipeBlock BASE_PIPE;
    public static ForceTubeBlock FORCE_TUBE;
    public static ForceControllerBlock FORCE_CONTROLLER;
    public static ForceModifierBlock FORCE_MODIFIER;

    private BlockInit() {}

    public static void registerAll(RegistryEvent.Register<Block> event) {
        /*BASE_PIPE = register("base_pipe", new BasePipeBlock(AbstractBlock.Properties.of(Material.GLASS)
                .dynamicShape()
                .strength(1.0f, 2.0f)
                .noOcclusion()
                .isViewBlocking(BlockInit::never)
        ));*/

    FORCE_TUBE = register("force_tube", new ForceTubeBlock(AbstractBlock.Properties.of(Material.GLASS)
            .dynamicShape()
            .strength(1.0f)
            .noOcclusion()
            .isViewBlocking(BlockInit::never)
            //.noCollission()
            //.speedFactor(0.2F)
            .harvestTool(ToolType.PICKAXE)
            .requiresCorrectToolForDrops()
            .lightLevel(enabledBlockEmission(3))
            //.hasPostProcess(BlockInit::always)
            .emissiveRendering(BlockInit::always)
            .randomTicks()
    ));

    FORCE_CONTROLLER = register("force_controller", new ForceControllerBlock(AbstractBlock.Properties.of(Material.STONE)
            .strength(3.5F)
            .requiresCorrectToolForDrops()
            .harvestTool(ToolType.PICKAXE)
    ));

    FORCE_MODIFIER = register("force_modifier", new ForceModifierBlock(AbstractBlock.Properties.of(Material.STONE)
            .strength(3.5F)
            .requiresCorrectToolForDrops()
            .harvestTool(ToolType.PICKAXE)
    ));

        //RenderLayerHandler.setRenderType(BASE_PIPE, RenderLayerHandler.RenderTypeSkeleton.CUTOUT_MIPPED);
        //initializeSpawnEggs();
    }

    private static <T extends Block> T register(String name, T block) {
        ResourceLocation id = AdvancedForcefields.getId(name);
        block.setRegistryName(id);
        ForgeRegistries.BLOCKS.register(block);
        return block;
    }

    private static boolean always(BlockState p_235426_0_, IBlockReader p_235426_1_, BlockPos p_235426_2_) {
        return true;
    }

    private static boolean never(BlockState p_235436_0_, IBlockReader p_235436_1_, BlockPos p_235436_2_) {
        return false;
    }

    private static ToIntFunction<BlockState> enabledBlockEmission(int p_235420_0_) {
        return (p_235421_1_) -> p_235421_1_.getValue(BlockStateProperties.ENABLED) ? p_235420_0_ : 0;
    }

    //public static void initializeSpawnEggs() {
    //    ModSpawnEggItem.initUnaddedEggs();
    //}
}

