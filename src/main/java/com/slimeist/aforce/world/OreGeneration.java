package com.slimeist.aforce.world;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.core.init.BlockInit;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

public class OreGeneration {

    public static final ArrayList<Holder<PlacedFeature>> OVERWORLD_ORES = new ArrayList<>();
    public static final ArrayList<Holder<PlacedFeature>> END_ORES = new ArrayList<>();
    public static final ArrayList<Holder<PlacedFeature>> NETHER_ORES = new ArrayList<>();

    public static final RuleTest END_STONE = new BlockMatchTest(Blocks.END_STONE);

    public static void registerOres() {
        Holder<PlacedFeature> enderite_ore = createOre("enderite_ore", END_STONE, BlockInit.ENDERITE_ORE.defaultBlockState(), 9, 0, 100, 24);
        END_ORES.add(enderite_ore);
    }

    private static Holder<PlacedFeature> createOre(String name, RuleTest fillerType, BlockState state, int veinSize, int minHeight, int maxHeight, int amountPerChunk) {
        /*return Feature.ORE.configured(new OreConfiguration(fillerType, state, veinSize))
                .rangeUniform(
                        VerticalAnchor.absolute(minHeight),
                        VerticalAnchor.absolute(maxHeight)
                ).squared().count(amountPerChunk);*/
        Holder<ConfiguredFeature<OreConfiguration, ?>> configured_feature = FeatureUtils.register(name, Feature.ORE, new OreConfiguration(fillerType, state, veinSize));
        return PlacementUtils.register(name + "_placed",
                configured_feature,
                OrePlacementHelper.commonOrePlacement(
                        amountPerChunk,
                        HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(minHeight),
                                VerticalAnchor.absolute(maxHeight)
                        )
                ));
    }

    @Mod.EventBusSubscriber(modid = AdvancedForcefields.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusSubscriber {
        @SubscribeEvent
        public static void biomeLoading(BiomeLoadingEvent event) {
            List<Holder<PlacedFeature>> features = event.getGeneration().getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES);
            switch (event.getCategory()) {
                case NETHER -> features.addAll(OreGeneration.NETHER_ORES);
                case THEEND -> features.addAll(OreGeneration.END_ORES);
                default -> features.addAll(OreGeneration.OVERWORLD_ORES);
            }
        }
    }
}
