package com.slimeist.aforce.core.init;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeTabInit {

    public static final CreativeModeTab FORCE_NETWORK = new CreativeModeTab("force_network") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemInit.FORCE_CONTROLLER);
        }
    };
}


