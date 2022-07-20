package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.blocks.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.EntityType;
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
    public static AdvancedForceModifierBlock ADVANCED_FORCE_MODIFIER;
    public static OreBlock ENDERITE_ORE;
    public static Block ENDERITE_BLOCK;

    private BlockInit() {
    }

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
                .sound(SoundType.GLASS)
                .isViewBlocking(BlockInit::never)
                .isValidSpawn(BlockInit::never)
                .isRedstoneConductor(BlockInit::never)
                .isSuffocating(BlockInit::never)
                //.noCollission()
                //.speedFactor(0.2F)
                .harvestTool(ToolType.PICKAXE)
                .requiresCorrectToolForDrops()
                .lightLevel(enabledBlockEmission(3))
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
                .sound(SoundType.LODESTONE)
        ));

        ADVANCED_FORCE_MODIFIER = register("advanced_force_modifier", new AdvancedForceModifierBlock(AbstractBlock.Properties.of(Material.STONE)
                .strength(3.5F)
                .requiresCorrectToolForDrops()
                .harvestTool(ToolType.PICKAXE)
                .sound(SoundType.LODESTONE)
        ));

        ENDERITE_ORE = register("enderite_ore", new CustomOreBlock(AbstractBlock.Properties.of(Material.STONE, MaterialColor.SAND)
                .requiresCorrectToolForDrops()
                .strength(3.0F, 9.0F)
        ));

        ENDERITE_BLOCK = register("enderite_block", new Block(AbstractBlock.Properties.of(Material.METAL, MaterialColor.COLOR_GREEN)
                .requiresCorrectToolForDrops()
                .strength(25.0F, 600.0F)
                .sound(SoundType.NETHERITE_BLOCK)
        ));

        // RenderLayerHandler.setRenderType(BASE_PIPE, RenderLayerHandler.RenderTypeSkeleton.CUTOUT_MIPPED);
        // initializeSpawnEggs();
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

    private static boolean never(BlockState p_235427_0_, IBlockReader p_235427_1_, BlockPos p_235427_2_, EntityType<?> p_235427_3_) {
        return false;
    }

    private static boolean always(BlockState p_235437_0_, IBlockReader p_235437_1_, BlockPos p_235437_2_, EntityType<?> p_235437_3_) {
        return true;
    }

    private static ToIntFunction<BlockState> enabledBlockEmission(int p_235420_0_) {
        return (p_235421_1_) -> p_235421_1_.getValue(BlockStateProperties.ENABLED) ? p_235420_0_ : 0;
    }

    //public static void initializeSpawnEggs() {
    //    ModSpawnEggItem.initUnaddedEggs();
    //}
}

