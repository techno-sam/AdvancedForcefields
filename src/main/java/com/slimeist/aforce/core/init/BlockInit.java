package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.blocks.CustomOreBlock;
import com.slimeist.aforce.common.blocks.ForceControllerBlock;
import com.slimeist.aforce.common.blocks.ForceModifierBlock;
import com.slimeist.aforce.common.blocks.ForceTubeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
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
    public static Block RAW_ENDERITE_BLOCK;

    private BlockInit() {
    }

    public static void registerAll(RegistryEvent.Register<Block> event) {
        /*BASE_PIPE = register("base_pipe", new BasePipeBlock(AbstractBlock.Properties.of(Material.GLASS)
                .dynamicShape()
                .strength(1.0f, 2.0f)
                .noOcclusion()
                .isViewBlocking(BlockInit::never)
        ));*/

        FORCE_TUBE = register("force_tube", new ForceTubeBlock(BlockBehaviour.Properties.of(Material.GLASS)
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
                .requiresCorrectToolForDrops()
                .lightLevel(enabledBlockEmission(3))
                .emissiveRendering(BlockInit::always)
                .randomTicks()
        ));

        FORCE_CONTROLLER = register("force_controller", new ForceControllerBlock(BlockBehaviour.Properties.of(Material.STONE)
                .strength(3.5F)
                .requiresCorrectToolForDrops()
        ));

        FORCE_MODIFIER = register("force_modifier", new ForceModifierBlock(BlockBehaviour.Properties.of(Material.STONE)
                .strength(3.5F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.LODESTONE)
        ));

        ADVANCED_FORCE_MODIFIER = register("advanced_force_modifier", new AdvancedForceModifierBlock(AbstractBlock.Properties.of(Material.STONE)
                .strength(3.5F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.LODESTONE)
        ));

        ENDERITE_ORE = register("enderite_ore", new CustomOreBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SAND)
                .requiresCorrectToolForDrops()
                .strength(3.0F, 9.0F),
                UniformInt.of(0, 0)
        ));

        ENDERITE_BLOCK = register("enderite_block", new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_GREEN)
                .requiresCorrectToolForDrops()
                .strength(17.0F, 408.0F)
                .sound(SoundType.NETHERITE_BLOCK)
        ));

        RAW_ENDERITE_BLOCK = register("raw_enderite_block", new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_GREEN)
                .requiresCorrectToolForDrops()
                .strength(15.0F, 360.0F)
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

    private static boolean always(BlockState p_235426_0_, BlockGetter p_235426_1_, BlockPos p_235426_2_) {
        return true;
    }

    private static boolean never(BlockState p_235436_0_, BlockGetter p_235436_1_, BlockPos p_235436_2_) {
        return false;
    }

    private static boolean never(BlockState p_235427_0_, BlockGetter p_235427_1_, BlockPos p_235427_2_, EntityType<?> p_235427_3_) {
        return false;
    }

    private static boolean always(BlockState p_235437_0_, BlockGetter p_235437_1_, BlockPos p_235437_2_, EntityType<?> p_235437_3_) {
        return true;
    }

    private static ToIntFunction<BlockState> enabledBlockEmission(int p_235420_0_) {
        return (p_235421_1_) -> p_235421_1_.getValue(BlockStateProperties.ENABLED) ? p_235420_0_ : 0;
    }

    //public static void initializeSpawnEggs() {
    //    ModSpawnEggItem.initUnaddedEggs();
    //}
}
