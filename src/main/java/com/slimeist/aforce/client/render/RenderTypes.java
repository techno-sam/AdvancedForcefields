package com.slimeist.aforce.client.render;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;

public class RenderTypes extends RenderType {
    public RenderTypes(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    public static RenderType simpleForceField(ResourceLocation texture) {
        RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        return create("simple_force_field", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, false, rendertype$state);
    }
}
