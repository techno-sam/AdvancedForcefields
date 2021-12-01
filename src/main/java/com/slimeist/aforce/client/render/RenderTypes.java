package com.slimeist.aforce.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

public class RenderTypes extends RenderType {
    public RenderTypes(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_) {
        super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
    }

    public static RenderType entityTranslucent(ResourceLocation p_228644_0_) {
        return entityTranslucent(p_228644_0_, true);
    }

    public static RenderType entityTranslucent(ResourceLocation p_230168_0_, boolean p_230168_1_) {
        RenderType.State rendertype$state = RenderType.State.builder().setTextureState(new RenderState.TextureState(p_230168_0_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(p_230168_1_);
        return create("entity_translucent", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, true, rendertype$state);
    }

    public static RenderType beaconBeam(ResourceLocation p_228637_0_, boolean p_228637_1_) {
        RenderType.State rendertype$state = RenderType.State.builder().setTextureState(new RenderState.TextureState(p_228637_0_, false, false)).setTransparencyState(p_228637_1_ ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY).setWriteMaskState(p_228637_1_ ? COLOR_WRITE : COLOR_DEPTH_WRITE).setFogState(NO_FOG)
                .setOutputState(MAIN_TARGET)
                .createCompositeState(false);
        return create("beacon_beam", DefaultVertexFormats.BLOCK, 7, 256, false, true, rendertype$state);
    }

    public static RenderType beaconBeam(ResourceLocation resource) {
        return beaconBeam(resource, true);
    }

    public static RenderType forceField(ResourceLocation texture) {
        RenderType.State rendertype$state = RenderType.State.builder().setTextureState(new RenderState.TextureState(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY) //from beacon
                .setWriteMaskState(COLOR_WRITE) //from beacon
                .setDiffuseLightingState(DIFFUSE_LIGHTING)
                .setAlphaState(DEFAULT_ALPHA)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOutputState(TRANSLUCENT_TARGET)
                .setFogState(NO_FOG) //from beacon
                .createCompositeState(false); //from beacon
        return create("force_field", DefaultVertexFormats.NEW_ENTITY, 7, 256, false, false, rendertype$state);
    }

    public static RenderType simpleForceField(ResourceLocation texture) {
        RenderType.State rendertype$state = RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setAlphaState(DEFAULT_ALPHA)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        return create("simple_force_field", DefaultVertexFormats.BLOCK, 7, 256, false, false, rendertype$state);
    }
}
