package com.slimeist.aforce.client.render.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.client.render.RenderTypes;
import com.slimeist.aforce.client.render.model.powah_models.AbstractModel;
import com.slimeist.aforce.client.render.tileentity.ForceTubeTileEntityRenderer;
import com.slimeist.aforce.common.tiles.ForceTubeTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class ForceFieldModel extends AbstractModel<ForceTubeTileEntity, ForceTubeTileEntityRenderer> {
    public static final ResourceLocation FORCE_FIELD_RESOURCE_LOCATION = AdvancedForcefields.getId("textures/entity/force_field.png");
    private final SidedModelRenderer forceCube;

    public ForceFieldModel() {
        super(RenderTypes::forceField);
        int pixels = 16;
        this.texWidth = pixels * 4;
        this.texHeight = pixels * 2;
        this.forceCube = new SidedModelRenderer(this, 0, 0);
        float offset = -(pixels / 2.0F);
        this.forceCube.addBox(offset, offset, offset, pixels, pixels, pixels);
        this.forceCube.setPos(0F, 0F, 0F);
        this.forceCube.setTexSize(this.texWidth, this.texHeight);
        this.forceCube.mirror = true;
        /*this.forceCube.setSideVisibility(Direction.UP, true);    //maps to: DOWN
        this.forceCube.setSideVisibility(Direction.DOWN, false);  //maps to: UP
        this.forceCube.setSideVisibility(Direction.NORTH, true); //maps to: SOUTH
        this.forceCube.setSideVisibility(Direction.SOUTH, false); //maps to: NORTH
        this.forceCube.setSideVisibility(Direction.EAST, true);  //maps to: EAST
        this.forceCube.setSideVisibility(Direction.WEST, false);  //maps to: WEST*/
        //this.setRotation(this.forceCube, 0.0f, 0.0f, 45.0f);
    }

    public ForceFieldModel setSideVis(Direction direction, boolean visible) {
        this.forceCube.setSideVisibility(direction, visible);
        return this;
    }

    public void updateSideVisibility(ForceTubeTileEntity te) {
        for (Direction dir : Direction.values()) {
            this.setSideVis(dir, !te.isConnected(dir.getOpposite()));
        }
    }

    @Override
    public void renderToBuffer(ForceTubeTileEntity te, ForceTubeTileEntityRenderer renderer, MatrixStack matrix, IRenderTypeBuffer rtb, int light, int ov) {
        this.renderToBuffer(te, renderer, matrix, rtb, light, ov, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void renderToBuffer(ForceTubeTileEntity te, ForceTubeTileEntityRenderer renderer, MatrixStack matrix, IRenderTypeBuffer rtb, int light, int ov, float red, float green, float blue, float alpha) {
        this.updateSideVisibility(te);
        IVertexBuilder buffer = rtb.getBuffer(renderType(FORCE_FIELD_RESOURCE_LOCATION));
        this.forceCube.render(matrix, buffer, light, ov, red, green, blue, alpha);
    }
}
