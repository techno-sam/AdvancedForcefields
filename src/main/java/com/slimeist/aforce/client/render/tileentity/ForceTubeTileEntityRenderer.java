package com.slimeist.aforce.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.client.render.model.ForceFieldModel;
import com.slimeist.aforce.client.util.ClientUtils;
import com.slimeist.aforce.common.tiles.ForceTubeTileEntity;
import com.slimeist.aforce.core.init.BlockInit;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

//structure from https://github.com/TheGreyGhost/MinecraftByExample/blob/master/src/main/java/minecraftbyexample/mbe21_tileentityrenderer/TileEntityRendererMBE21.java

@OnlyIn(Dist.CLIENT)
public class ForceTubeTileEntityRenderer extends TileEntityRenderer<ForceTubeTileEntity> {

    private static final ForceFieldModel FORCE_FIELD_MODEL = new ForceFieldModel();

    public ForceTubeTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
        super(p_i226006_1_);
    }

    @Override
    public void render(ForceTubeTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffers, int combinedLight, int combinedOverlay) {
        //render distance
        int distance = tile.getDistance();
        matrixStack.pushPose();

        float forward = 1.01f;
        if (distance==-1) {
            forward = 0.76f;
        }

        final Vector3d TRANSLATION_OFFSET = new Vector3d(0.5, 0.5, forward);
        matrixStack.translate(TRANSLATION_OFFSET.x, TRANSLATION_OFFSET.y, TRANSLATION_OFFSET.z); // translate

        matrixStack.scale(0.010416667F, -0.010416667F, 0.010416667F);

        int r = 255;
        int g = 255;
        int b = 0;
        FontRenderer fontrenderer = this.renderer.getFont(); //          X                  Y                COLOR
        fontrenderer.draw(matrixStack, "Dist: "+distance, 0.0f, 0.0f, NativeImage.combine(0, r, g, b));
        matrixStack.popPose();

        //render force field
        if (distance>0) { //we are active
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.5, 0.5);
            matrixStack.scale(1.0f, -1.0f, -1.0f);
            FORCE_FIELD_MODEL.renderToBuffer(tile, this, matrixStack, renderBuffers, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.popPose();
        }
    }

    // this should be true for tileentities which render globally (no render bounding box), such as beacons.
    @Override
    public boolean shouldRenderOffScreen(ForceTubeTileEntity tile)
    {
        return false;
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(TileEntityTypeInit.FORCE_TUBE_TYPE, ForceTubeTileEntityRenderer::new);
    }


    private static final Logger LOGGER = LogManager.getLogger();
}
