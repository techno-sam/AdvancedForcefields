package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.modifier_actions.*;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;

public final class ModifierInit {
    public static ForceModifierRegistry SLIME_ACTION;
    public static ForceModifierRegistry DEFAULT_ACTION;
    public static ForceModifierRegistry PASS_THROUGH_ACTION;
    public static ForceModifierRegistry LINGERING_POTION_ACTION;
    public static ForceModifierRegistry SHULKER_LEVITATION_ACTION;

    private ModifierInit() {}

    public static void registerAll(RegistryEvent.Register<ForceModifierRegistry> event) {
        SLIME_ACTION = register("slime_action", new ForceModifierRegistry(Items.SLIME_BLOCK, new BouncyAction(0.2d)));
        DEFAULT_ACTION = register("default_action", new ForceModifierRegistry(Items.COBBLESTONE, new BlockAction()));
        PASS_THROUGH_ACTION = register("pass_through_action", new ForceModifierRegistry(Items.OAK_TRAPDOOR, new PassThroughAction()));
        LINGERING_POTION_ACTION = register("lingering_potion_action", new ForceModifierRegistry(Items.LINGERING_POTION, new LingeringPotionAction()));
        SHULKER_LEVITATION_ACTION = register("shulker_levitation_action", new ForceModifierRegistry(Items.SHULKER_SHELL, new ShulkerLevitationAction()));
    }

    private static <T extends ForceModifierRegistry> T register(String name, T modifier) {
        ResourceLocation id = AdvancedForcefields.getId(name);
        modifier.setRegistryName(id);
        RegistryInit.MODIFIER_REGISTRY.register(modifier);
        return modifier;
    }
}
