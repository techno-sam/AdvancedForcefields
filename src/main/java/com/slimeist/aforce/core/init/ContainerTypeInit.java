package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.containers.force_controller.ContainerForceController;
import com.slimeist.aforce.common.containers.force_modifier.ContainerAdvancedForceModifier;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class ContainerTypeInit {
    public static ContainerType<ContainerForceController> FORCE_CONTROLLER_TYPE;
    public static ContainerType<ContainerForceModifier> FORCE_MODIFIER_TYPE;
    public static ContainerType<ContainerAdvancedForceModifier> ADVANCED_FORCE_MODIFIER_TYPE;

    private ContainerTypeInit() {}

    public static void registerAll(RegistryEvent.Register<ContainerType<?>> event) {
        FORCE_CONTROLLER_TYPE = register("force_controller", IForgeContainerType.create(ContainerForceController::createContainerClientSide));
        FORCE_MODIFIER_TYPE = register("force_modifier", IForgeContainerType.create(ContainerForceModifier::createContainerClientSide));
        ADVANCED_FORCE_MODIFIER_TYPE = register("advanced_force_modifier", IForgeContainerType.create(ContainerAdvancedForceModifier::createContainerClientSide));
    }

    private static <T extends ContainerType<?>> T register(String name, T containertype) {
        ResourceLocation id = AdvancedForcefields.getId(name);
        containertype.setRegistryName(id);
        ForgeRegistries.CONTAINERS.register(containertype);
        return containertype;
    }
}
