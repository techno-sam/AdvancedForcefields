package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.tiles.ForceControllerTileEntity;
import com.slimeist.aforce.common.tiles.ForceModifierTileEntity;
import com.slimeist.aforce.common.tiles.ForceTubeTileEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class TileEntityTypeInit {

    public static BlockEntityType<ForceTubeTileEntity> FORCE_TUBE_TYPE;
    public static BlockEntityType<ForceControllerTileEntity> FORCE_CONTROLLER_TYPE;
    public static BlockEntityType<ForceModifierTileEntity> FORCE_MODIFIER_TYPE;

    private TileEntityTypeInit() {}

    public static void registerAll(RegistryEvent.Register<BlockEntityType<?>> event) {
        FORCE_TUBE_TYPE = register("force_tube", BlockEntityType.Builder.of(ForceTubeTileEntity::new, BlockInit.FORCE_TUBE).build(null));
        FORCE_CONTROLLER_TYPE = register("force_controller", BlockEntityType.Builder.of(ForceControllerTileEntity::new, BlockInit.FORCE_CONTROLLER).build(null));
        FORCE_MODIFIER_TYPE = register("force_modifier", BlockEntityType.Builder.of(ForceModifierTileEntity::new, BlockInit.FORCE_MODIFIER).build(null));
    }

    private static <T extends BlockEntityType<?>> T register(String name, T tileentitytype) {
        ResourceLocation id = AdvancedForcefields.getId(name);
        tileentitytype.setRegistryName(id);
        ForgeRegistries.BLOCK_ENTITIES.register(tileentitytype);
        return tileentitytype;
    }
}
