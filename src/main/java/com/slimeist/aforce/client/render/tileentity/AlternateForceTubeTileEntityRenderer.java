package com.slimeist.aforce.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.slimeist.aforce.client.render.RenderTypes;
import com.slimeist.aforce.common.tiles.ForceTubeTileEntity;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class AlternateForceTubeTileEntityRenderer extends TileEntityRenderer<ForceTubeTileEntity> {

    public static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");

    public AlternateForceTubeTileEntityRenderer(TileEntityRendererDispatcher dispatch) {
        super(dispatch);
    }

    /*@Override
    public void render(ForceTubeTileEntity te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer renderBuffer, int combinedLight, int combinedOverlay) {
        matrix.pushPose();

        IVertexBuilder builder = renderBuffer.getBuffer(RenderTypes.simpleForceField(BEAM_LOCATION));

        matrix.popPose();
    }*/

    public void render(ForceTubeTileEntity te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer rtbuffer, int combinedLight, int combinedOverlay) {
        long i = te.getLevel().getGameTime();
        int baseY = 0;
        int height = 1;
        float[] color = new float[]{0.5f, 0.0f, 1.0f};

        //renderBeaconBeam(matrix, rtbuffer, partialTicks, i, baseY, height, color);

        float red = 1.0f;
        float green = 0.1f;
        float blue = 0.8f;
        float alpha = 0.7f;

        int minY = 0;
        int maxY = 1;

        float x1 = 0.0f;
        float z1 = 0.0f;

        float x2 = 1.0f;
        float z2 = 0.0f;

        float x3 = 0.0f;
        float z3 = 1.0f;

        float x4 = 1.0f;
        float z4 = 1.0f;

        float u1 = 0.0f;
        float v1 = 1.0f;

        float u2 = 1.0f;
        float v2 = 2.0f;

        renderPart(matrix, rtbuffer.getBuffer(RenderTypes.simpleForceField(BEAM_LOCATION)), red, green, blue, alpha, minY, maxY, x1, z1, x2, z2, x3, z3, x4, z4, u1, v1, u2, v2);
    }

    private static void renderBeaconBeam(MatrixStack matrix, IRenderTypeBuffer rtbuffer, float partialTicks, long gameTime, int baseY, int height, float[] color) {
        renderBeaconBeam(matrix, rtbuffer, BEAM_LOCATION, partialTicks, 1.0F, gameTime, baseY, height, color, 0.2F, 0.25F);
    }

    public static void renderBeaconBeam(MatrixStack matrix, IRenderTypeBuffer rtbuffer, ResourceLocation texture, float partialTicks, float unknown, long gameTime, int baseY, int height, float[] color, float coreWidth, float outerWidth) {
        int topY = baseY + height;
        matrix.pushPose();
        matrix.translate(0.5D, 0.0D, 0.5D);
        float f = (float)Math.floorMod(gameTime, 40L) + partialTicks;
        float f1 = height < 0 ? f : -f;
        float f2 = MathHelper.frac(f1 * 0.2F - (float)MathHelper.floor(f1 * 0.1F));
        float red = color[0];
        float green = color[1];
        float blue = color[2];
        matrix.pushPose();
        matrix.mulPose(Vector3f.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f6 = 0.0F;
        float f8 = 0.0F;
        float f9 = -coreWidth;
        float f10 = 0.0F;
        float f11 = 0.0F;
        float f12 = -coreWidth;
        float f13 = 0.0F;
        float f14 = 1.0F;
        float f15 = -1.0F + f2;
        float f16 = (float)height * unknown * (0.5F / coreWidth) + f15;
        renderPart(matrix, rtbuffer.getBuffer(RenderType.beaconBeam(texture, false)), red, green, blue, 1.0F, baseY, topY, 0.0F, coreWidth, coreWidth, 0.0F, f9, 0.0F, 0.0F, f12, 0.0F, 1.0F, f16, f15);
        matrix.popPose();
        f6 = -outerWidth;
        float f7 = -outerWidth;
        f8 = -outerWidth;
        f9 = -outerWidth;
        f13 = 0.0F;
        f14 = 1.0F;
        f15 = -1.0F + f2;
        f16 = (float)height * unknown + f15;
        renderPart(matrix, rtbuffer.getBuffer(RenderType.beaconBeam(texture, true)), red, green, blue, 0.125F, baseY, topY, f6, f7, outerWidth, f8, f9, outerWidth, outerWidth, outerWidth, 0.0F, 1.0F, f16, f15);
        matrix.popPose();
    }

    private static void renderPart(MatrixStack matrix, IVertexBuilder builder, float red, float green, float blue, float alpha, int minY, int maxY, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float u1, float v1, float u2, float v2) {
        MatrixStack.Entry matrixstack$entry = matrix.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();
        renderQuad(matrix4f, matrix3f, builder, red, green, blue, alpha, minY, maxY, x1, z1, x2, z2, u1, v1, u2, v2);
        renderQuad(matrix4f, matrix3f, builder, red, green, blue, alpha, minY, maxY, x4, z4, x3, z3, u1, v1, u2, v2);
        renderQuad(matrix4f, matrix3f, builder, red, green, blue, alpha, minY, maxY, x2, z2, x4, z4, u1, v1, u2, v2);
        renderQuad(matrix4f, matrix3f, builder, red, green, blue, alpha, minY, maxY, x3, z3, x1, z1, u1, v1, u2, v2);
    }

    private static void renderQuad(Matrix4f poseMatrix, Matrix3f normalMatrix, IVertexBuilder builder, float red, float green, float blue, float alpha, int y1, int y2, float x1, float z1, float x2, float z2, float u2, float u1, float v1, float v2) {
        addVertex(poseMatrix, normalMatrix, builder, red, green, blue, alpha, y2, x1, z1, u1, v1);
        addVertex(poseMatrix, normalMatrix, builder, red, green, blue, alpha, y1, x1, z1, u1, v2);
        addVertex(poseMatrix, normalMatrix, builder, red, green, blue, alpha, y1, x2, z2, u2, v2);
        addVertex(poseMatrix, normalMatrix, builder, red, green, blue, alpha, y2, x2, z2, u2, v1);
    }

    private static void addVertex(Matrix4f poseMatrix, Matrix3f normalMatrix, IVertexBuilder builder, float red, float green, float blue, float alpha, int y, float x, float z, float u, float v) {
        builder.vertex(poseMatrix, x, (float)y, z).color(red, green, blue, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
    }

    //@Override
    public boolean shouldRenderOffScreen(ForceTubeTileEntity tile)
    {
        return true;
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(TileEntityTypeInit.FORCE_TUBE_TYPE, AlternateForceTubeTileEntityRenderer::new);
    }


    private static final Logger LOGGER = LogManager.getLogger();
}
