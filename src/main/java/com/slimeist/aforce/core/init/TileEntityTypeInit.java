package com.slimeist.aforce.core.init;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.tiles.ForceControllerTileEntity;
import com.slimeist.aforce.common.tiles.ForceModifierTileEntity;
import com.slimeist.aforce.common.tiles.ForceTubeTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class TileEntityTypeInit {

    public static TileEntityType<ForceTubeTileEntity> FORCE_TUBE_TYPE;
    public static TileEntityType<ForceControllerTileEntity> FORCE_CONTROLLER_TYPE;
    public static TileEntityType<ForceModifierTileEntity> FORCE_MODIFIER_TYPE;

    private TileEntityTypeInit() {}

    public static void registerAll(RegistryEvent.Register<TileEntityType<?>> event) {
        FORCE_TUBE_TYPE = register("force_tube", TileEntityType.Builder.of(ForceTubeTileEntity::new, BlockInit.FORCE_TUBE).build(null));
        FORCE_CONTROLLER_TYPE = register("force_controller", TileEntityType.Builder.of(ForceControllerTileEntity::new, BlockInit.FORCE_CONTROLLER).build(null));
        FORCE_MODIFIER_TYPE = register("force_modifier", TileEntityType.Builder.of(ForceModifierTileEntity::new, BlockInit.FORCE_MODIFIER).build(null));
    }

    private static <T extends TileEntityType<?>> T register(String name, T tileentitytype) {
        ResourceLocation id = AdvancedForcefields.getId(name);
        tileentitytype.setRegistryName(id);
        ForgeRegistries.TILE_ENTITIES.register(tileentitytype);
        return tileentitytype;
    }
}
