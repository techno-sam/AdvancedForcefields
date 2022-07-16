package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.modifier_actions.*;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;

public final class ModifierInit {
    public static ForceModifierRegistry SLIME_ACTION;
    public static ForceModifierRegistry DEFAULT_ACTION;
    public static ForceModifierRegistry PASS_THROUGH_ACTION;
    public static ForceModifierRegistry LINGERING_POTION_ACTION;
    public static ForceModifierRegistry SHULKER_LEVITATION_ACTION;
    public static ForceModifierRegistry MAGMA_ACTION;
    public static ForceModifierRegistry BLAZE_FIRE_ACTION;

    private ModifierInit() {}

    public static void registerAll(RegistryEvent.Register<ForceModifierRegistry> event) {
        SLIME_ACTION = register("slime_action", new ForceModifierRegistry(Items.SLIME_BLOCK, new BouncyAction(0.2d)));
        DEFAULT_ACTION = register("default_action", new ForceModifierRegistry(Items.COBBLESTONE, new BlockAction()));
        PASS_THROUGH_ACTION = register("pass_through_action", new ForceModifierRegistry(ItemTags.TRAPDOORS, new PassThroughAction()));
        LINGERING_POTION_ACTION = register("lingering_potion_action", new ForceModifierRegistry(Items.LINGERING_POTION, new LingeringPotionAction()));
        SHULKER_LEVITATION_ACTION = register("shulker_levitation_action", new ForceModifierRegistry(Items.SHULKER_SHELL, new ShulkerLevitationAction()));
        MAGMA_ACTION = register("magma_action", new ForceModifierRegistry(Items.MAGMA_BLOCK, new MagmaAction()));
        BLAZE_FIRE_ACTION = register("blaze_fire_action", new ForceModifierRegistry(Items.BLAZE_ROD, new BlazeFireAction(1.0f)));
    }

    private static <T extends ForceModifierRegistry> T register(String name, T modifier) {
        ResourceLocation id = AdvancedForcefields.getId(name);
        modifier.setRegistryName(id);
        RegistryInit.MODIFIER_REGISTRY.register(modifier);
        return modifier;
    }
}
