package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.modifier_actions.BlockAction;
import com.slimeist.aforce.common.modifier_actions.BouncyAction;
import com.slimeist.aforce.common.modifier_actions.PassThroughAction;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModifierInit {
    public static ForceModifierRegistry SLIME_ACTION;
    public static ForceModifierRegistry DEFAULT_ACTION;
    public static ForceModifierRegistry PASS_THROUGH_ACTION;

    private ModifierInit() {}

    public static void registerAll(RegistryEvent.Register<ForceModifierRegistry> event) {
        SLIME_ACTION = register("slime_action", new ForceModifierRegistry(Items.SLIME_BLOCK, new BouncyAction(0.2d)));
        DEFAULT_ACTION = register("default_action", new ForceModifierRegistry(Items.BARRIER, new BlockAction()));
        PASS_THROUGH_ACTION = register("pass_through_action", new ForceModifierRegistry(Items.OAK_TRAPDOOR, new PassThroughAction()));
    }

    private static <T extends ForceModifierRegistry> T register(String name, T modifier) {
        ResourceLocation id = AdvancedForcefields.getId(name);
        modifier.setRegistryName(id);
        RegistryInit.MODIFIER_REGISTRY.register(modifier);
        return modifier;
    }
}
