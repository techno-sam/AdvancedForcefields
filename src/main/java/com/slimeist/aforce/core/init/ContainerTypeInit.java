package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.containers.force_controller.ContainerForceController;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class ContainerTypeInit {
    public static MenuType<ContainerForceController> FORCE_CONTROLLER_TYPE;
    public static MenuType<ContainerForceModifier> FORCE_MODIFIER_TYPE;

    private ContainerTypeInit() {}

    public static void registerAll(RegistryEvent.Register<MenuType<?>> event) {
        FORCE_CONTROLLER_TYPE = register("force_controller", IForgeContainerType.create(ContainerForceController::createContainerClientSide));
        FORCE_MODIFIER_TYPE = register("force_modifier", IForgeContainerType.create(ContainerForceModifier::createContainerClientSide));
    }

    private static <T extends MenuType<?>> T register(String name, T containertype) {
        ResourceLocation id = AdvancedForcefields.getId(name);
        containertype.setRegistryName(id);
        ForgeRegistries.CONTAINERS.register(containertype);
        return containertype;
    }
}
