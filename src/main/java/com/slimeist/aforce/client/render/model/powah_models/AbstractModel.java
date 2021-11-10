package com.slimeist.aforce.client.render.model.powah_models;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public abstract class AbstractModel<T extends TileEntity, R extends TileEntityRenderer<T>> extends EmptyModel {
    public AbstractModel(Function<ResourceLocation, RenderType> function) {
        super(function);
    }

    public abstract void renderToBuffer(T te, R renderer, MatrixStack matrix, IRenderTypeBuffer rtb, int light, int ov);
    public abstract void renderToBuffer(T te, R renderer, MatrixStack matrix, IRenderTypeBuffer rtb, int light, int ov, float red, float green, float blue, float alpha);

    protected void setRotation(ModelRenderer model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}
