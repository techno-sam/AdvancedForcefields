package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class RegistryInit {
    public static IForgeRegistry<ForceModifierRegistry> MODIFIER_REGISTRY;

    public static void registerAll(RegistryEvent.NewRegistry event) {
        RegistryBuilder<ForceModifierRegistry> registryBuilder = new RegistryBuilder<>();
        registryBuilder.setName(AdvancedForcefields.getId("modifier_registry"));
        registryBuilder.setType(ForceModifierRegistry.class);
        registryBuilder.setDefaultKey(AdvancedForcefields.getId("default_action"));
        MODIFIER_REGISTRY = registryBuilder.create();
    }
}
