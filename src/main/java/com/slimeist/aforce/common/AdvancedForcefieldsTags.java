package com.slimeist.aforce.common;

import com.slimeist.aforce.AdvancedForcefields;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class AdvancedForcefieldsTags {
    public static void init() {
        Blocks.init();
        Items.init();
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

    public static class Items {
        private static void init() {}

        public static final Tags.IOptionalNamedTag<Item> ENDER_FUEL = tag("ender_fuel");
        public static final Tags.IOptionalNamedTag<Item> MODIFIER_UPGRADE = tag("modifier_upgrade");

        private static Tags.IOptionalNamedTag<Item> tag(String name) {
            return ItemTags.createOptional(AdvancedForcefields.getId(name));
        }

        private static Tags.IOptionalNamedTag<Item> forgeTag(String name) {
            return ItemTags.createOptional(new ResourceLocation("forge", name));
        }
    }
}
