package com.slimeist.aforce.client.render.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.client.render.model.powah_models.AbstractModel;
import com.slimeist.aforce.client.render.tileentity.ForceTubeTileEntityRenderer;
import com.slimeist.aforce.common.tiles.ForceTubeTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class ForceFieldModel extends AbstractModel<ForceTubeTileEntity, ForceTubeTileEntityRenderer> {
    public static final ResourceLocation FORCE_FIELD_RESOURCE_LOCATION = AdvancedForcefields.getId("textures/entity/force_field.png");
    private final ModelRenderer forceCube;

    public ForceFieldModel() {
        super(RenderType::entityTranslucentCull);
        int pixels = 16;
        this.texWidth = pixels * 4;
        this.texHeight = pixels * 2;
        this.forceCube = new ModelRenderer(this, 0, 0);
        float offset = -(pixels / 2.0F);
        this.forceCube.addBox(offset, offset, offset, pixels, pixels, pixels);
        this.forceCube.setPos(0F, 0F, 0F);
        this.forceCube.setTexSize(this.texWidth, this.texHeight);
        this.forceCube.mirror = true;
        //this.setRotation(this.forceCube, 0.0f, 0.0f, 45.0f);
    }

    @Override
    public void renderToBuffer(ForceTubeTileEntity te, ForceTubeTileEntityRenderer renderer, MatrixStack matrix, IRenderTypeBuffer rtb, int light, int ov) {
        IVertexBuilder buffer = rtb.getBuffer(renderType(FORCE_FIELD_RESOURCE_LOCATION));
        this.forceCube.render(matrix, buffer, light, ov);
    }

    @Override
    public void renderToBuffer(ForceTubeTileEntity te, ForceTubeTileEntityRenderer renderer, MatrixStack matrix, IRenderTypeBuffer rtb, int light, int ov, float red, float green, float blue, float alpha) {
        IVertexBuilder buffer = rtb.getBuffer(renderType(FORCE_FIELD_RESOURCE_LOCATION));
        this.forceCube.render(matrix, buffer, light, ov, red, green, blue, alpha);
    }
}
