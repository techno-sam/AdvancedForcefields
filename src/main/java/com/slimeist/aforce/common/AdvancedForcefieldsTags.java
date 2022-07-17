package com.slimeist.aforce.common;

import com.slimeist.aforce.AdvancedForcefields;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;

public class AdvancedForcefieldsTags {
    public static void init() {
        Blocks.init();
        Items.init();
    }

    public static class Blocks {
        private static void init() {}

        public static final TagKey<Block> FORCE_COMPONENT = tag("force_component");
        public static final TagKey<Block> FORCE_COMPONENT_NO_CONTROLLER = tag("force_component_no_controller");
        public static final TagKey<Block> FORCE_TUBE = tag("force_tube");
        public static final TagKey<Block> FORCE_MODIFIER = tag("force_modifier");
        public static final TagKey<Block> FORCE_CONTROLLER = tag("force_controller");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(AdvancedForcefields.getId(name));
        }

        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }
    }

    public static class Items {
        private static void init() {}

        public static final TagKey<Item> ENDER_FUEL = tag("ender_fuel");
        public static final TagKey<Item> MODIFIER_UPGRADE = tag("modifier_upgrade");
        public static final TagKey<Item> SHIMMERING_HELMET = tag("shimmering_helmet");
        public static final TagKey<Item> SHIMMERING_CHESTPLATE = tag("shimmering_chestplate");
        public static final TagKey<Item> SHIMMERING_LEGGINGS = tag("shimmering_leggings");
        public static final TagKey<Item> SHIMMERING_BOOTS = tag("shimmering_boots");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(AdvancedForcefields.getId(name));
        }

        private static TagKey<Item> forgeTag(String name) {
            return ItemTags.create(new ResourceLocation("forge", name));
        }
    }
}
