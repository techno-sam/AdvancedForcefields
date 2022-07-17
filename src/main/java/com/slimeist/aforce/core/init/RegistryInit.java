package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class RegistryInit {
    public static Supplier<IForgeRegistry<ForceModifierRegistry>> MODIFIER_REGISTRY;

    public static void registerAll(NewRegistryEvent event) {
        RegistryBuilder<ForceModifierRegistry> registryBuilder = new RegistryBuilder<>();
        registryBuilder.setName(AdvancedForcefields.getId("modifier_registry"));
        registryBuilder.setType(ForceModifierRegistry.class);
        registryBuilder.setDefaultKey(AdvancedForcefields.getId("default_action"));
        MODIFIER_REGISTRY = event.create(registryBuilder);
    }
}
