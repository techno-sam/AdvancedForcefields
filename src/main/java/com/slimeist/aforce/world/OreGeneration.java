package com.slimeist.aforce.world;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.core.init.BlockInit;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OreGeneration {

    public static final ArrayList<ConfiguredFeature<?, ?>> OVERWORLD_ORES = new ArrayList<>();
    public static final ArrayList<ConfiguredFeature<?, ?>> END_ORES = new ArrayList<>();
    public static final ArrayList<ConfiguredFeature<?, ?>> NETHER_ORES = new ArrayList<>();

    public static final RuleTest END_STONE = new BlockMatchTest(Blocks.END_STONE);

    public static void registerOres() {
        ConfiguredFeature<?, ?> enderite_ore = createOre(END_STONE, BlockInit.ENDERITE_ORE.defaultBlockState(), 9, 0, 100, 24);
        END_ORES.add(register("enderite_ore", enderite_ore));
    }

    private static ConfiguredFeature<?, ?> createOre(RuleTest fillerType, BlockState state, int veinSize, int minHeight, int maxHeight, int amountPerChunk) {
        return Feature.ORE.configured(new OreConfiguration(fillerType, state, veinSize))
                .rangeUniform(
                        VerticalAnchor.absolute(minHeight),
                        VerticalAnchor.absolute(maxHeight)
                ).squared().count(amountPerChunk);
    }

    private static <Config extends FeatureConfiguration> ConfiguredFeature<Config, ?> register(String name, ConfiguredFeature<Config, ?> configuredFeature) {
        return Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, AdvancedForcefields.getId(name), configuredFeature);
    }

    @Mod.EventBusSubscriber(modid = AdvancedForcefields.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusSubscriber {
        @SubscribeEvent
        public static void biomeLoading(BiomeLoadingEvent event) {
            List<Supplier<ConfiguredFeature<?, ?>>> features = event.getGeneration().getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES);
            switch (event.getCategory()) {
                case NETHER -> OreGeneration.NETHER_ORES.forEach(ore -> features.add(() -> ore));
                case THEEND -> OreGeneration.END_ORES.forEach(ore -> features.add(() -> ore));
                default -> OreGeneration.OVERWORLD_ORES.forEach(ore -> features.add(() -> ore));
            }
        }
    }
}
