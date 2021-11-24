package com.slimeist.aforce.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.client.render.RenderTypes;
import com.slimeist.aforce.common.tiles.ForceTubeTileEntity;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AlternateForceTubeTileEntityRenderer extends TileEntityRenderer<ForceTubeTileEntity> {

    public static final ResourceLocation SHIMMER_LOCATION = AdvancedForcefields.getId("entity/force_field_shimmer");
    public static final RenderMaterial SHIMMER_MATERIAL = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, SHIMMER_LOCATION);

    public AlternateForceTubeTileEntityRenderer(TileEntityRendererDispatcher dispatch) {
        super(dispatch);
    }

    public void render(ForceTubeTileEntity te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer rtbuffer, int combinedLight, int combinedOverlay) {
        long gt = te.getLevel().getGameTime();

        float red = 1.0f;
        float green = 0.1f;
        float blue = 0.8f;
        float alpha = 0.7f;

        float x1 = 0.0f;
        float y1 = 0.0f;
        float z1 = 0.0f;

        float x2 = 0.0f;
        float y2 = 0.0f;
        float z2 = 0.0f;

        float u1 = 0.0f;
        float v1 = 0.0f;

        float u2 = 1.0f;
        float v2 = 1.0f;

        u1 = SHIMMER_MATERIAL.sprite().getU0();
        v1 = SHIMMER_MATERIAL.sprite().getV0();

        u2 = SHIMMER_MATERIAL.sprite().getU1();
        v2 = SHIMMER_MATERIAL.sprite().getV1();

        renderCube(matrix, SHIMMER_MATERIAL.buffer(rtbuffer, RenderTypes::simpleForceField), red, green, blue, alpha, x1, y1, z1, x2, y2, z2, u1, v1, u2, v2);
    }

    private void renderCube(MatrixStack matrix, IVertexBuilder builder, float red, float green, float blue, float alpha, float x1, float y1, float z1, float x2, float y2, float z2, float u1, float v1, float u2, float v2) {
        MatrixStack.Entry matrixstack$entry = matrix.last();
        Matrix4f poseMatrix = matrixstack$entry.pose();
        Matrix3f normalMatrix = matrixstack$entry.normal();

        this.renderFace(poseMatrix, normalMatrix, builder, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, red, green, blue, alpha, u1, v1, u2, v2);
        this.renderFace(poseMatrix, normalMatrix, builder, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, red, green, blue, alpha, u1, v1, u2, v2);
        this.renderFace(poseMatrix, normalMatrix, builder, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, red, green, blue, alpha, u1, v1, u2, v2);
        this.renderFace(poseMatrix, normalMatrix, builder, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, red, green, blue, alpha, u1, v1, u2, v2);
        this.renderFace(poseMatrix, normalMatrix, builder, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, red, green, blue, alpha, u1, v1, u2, v2);
        this.renderFace(poseMatrix, normalMatrix, builder, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, red, green, blue, alpha, u1, v1, u2, v2);
    }

    private void renderFace(Matrix4f poseMatrix, Matrix3f normalMatrix, IVertexBuilder builder, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4, float red, float green, float blue, float alpha, float u1, float v1, float u2, float v2) {
        /*builder.vertex(poseMatrix, x1, y1, z1).color(red, green, blue, alpha).endVertex();
        builder.vertex(poseMatrix, x2, y1, z2).color(red, green, blue, alpha).endVertex();
        builder.vertex(poseMatrix, x2, y2, z3).color(red, green, blue, alpha).endVertex();
        builder.vertex(poseMatrix, x1, y2, z4).color(red, green, blue, alpha).endVertex();*/
        vertex(poseMatrix, normalMatrix, builder, red, green, blue, alpha, x1, y1, z1, u2, v2);
        vertex(poseMatrix, normalMatrix, builder, red, green, blue, alpha, x2, y1, z2, u1, v2);
        vertex(poseMatrix, normalMatrix, builder, red, green, blue, alpha, x2, y2, z3, u1, v1);
        vertex(poseMatrix, normalMatrix, builder, red, green, blue, alpha, x1, y2, z4, u2, v1);
    }

    private static void vertex(Matrix4f poseMatrix, Matrix3f normalMatrix, IVertexBuilder builder, float red, float green, float blue, float alpha, float x, float y, float z, float u, float v) {
        builder.vertex(poseMatrix, x, y, z).color(red, green, blue, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(ForceTubeTileEntity tile)
    {
        return true;
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(TileEntityTypeInit.FORCE_TUBE_TYPE, AlternateForceTubeTileEntityRenderer::new);
    }


    private static final Logger LOGGER = LogManager.getLogger();
}
