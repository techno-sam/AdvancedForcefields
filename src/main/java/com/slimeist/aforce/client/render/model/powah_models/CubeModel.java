package com.slimeist.aforce.client.render.model.powah_models;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class CubeModel extends Model {
    private final ModelRenderer cube;

    public CubeModel(int pixels, Function<ResourceLocation, RenderType> type) {
        super(type);
        this.texWidth = pixels * 4;
        this.texHeight = pixels * 2;
        this.cube = new ModelRenderer(this, 0, 0);
        float offset = -(pixels / 2.0F);
        this.cube.addBox(offset, offset, offset, pixels, pixels, pixels);
        this.cube.setPos(0F, 0F, 0F);
        this.cube.setTexSize(this.texWidth, this.texHeight);
        this.cube.mirror = true;
    }

    @Override
    public void renderToBuffer(MatrixStack matrix, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.cube.render(matrix, buffer, packedLight, packedOverlay);
    }
}
