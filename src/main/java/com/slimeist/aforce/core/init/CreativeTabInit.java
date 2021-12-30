package com.slimeist.aforce.core.init;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class CreativeTabInit {

    public static final ItemGroup FORCE_NETWORK = new ItemGroup("force_network") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemInit.FORCE_CONTROLLER);
        }
    };
}


