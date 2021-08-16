package com.slimeist.aforce.common;

import com.slimeist.aforce.AdvancedForcefields;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class AdvancedForcefieldsTags {
    public static void init() {
        Blocks.init();
    }

    public static class Blocks {
        private static void init() {}

        public static final Tags.IOptionalNamedTag<Block> FORCE_COMPONENT = tag("force_component");
        public static final Tags.IOptionalNamedTag<Block> FORCE_COMPONENT_NO_CONTROLLER = tag("force_component_no_controller");
        public static final Tags.IOptionalNamedTag<Block> FORCE_TUBE = tag("force_tube");
        public static final Tags.IOptionalNamedTag<Block> FORCE_MODIFIER = tag("force_modifier");

        private static Tags.IOptionalNamedTag<Block> tag(String name) {
            return BlockTags.createOptional(AdvancedForcefields.getId(name));
        }

        private static Tags.IOptionalNamedTag<Block> forgeTag(String name) {
            return BlockTags.createOptional(new ResourceLocation("forge", name));
        }
    }
}
