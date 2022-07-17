/*package com.slimeist.aforce.client.render.tileentity;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.slimeist.aforce.client.render.model.ForceFieldModel;
import com.slimeist.aforce.common.tiles.ForceTubeTileEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//structure from https://github.com/TheGreyGhost/MinecraftByExample/blob/master/src/main/java/minecraftbyexample/mbe21_tileentityrenderer/TileEntityRendererMBE21.java

@OnlyIn(Dist.CLIENT)
public class ForceTubeTileEntityRenderer implements BlockEntityRenderer<ForceTubeTileEntity> {

    private static final ForceFieldModel FORCE_FIELD_MODEL = new ForceFieldModel();

    private BlockEntityRendererProvider.Context context;

    public ForceTubeTileEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(ForceTubeTileEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource renderBuffers, int combinedLight, int combinedOverlay) {
        //render distance
        int distance = tile.getDistance();
        matrixStack.pushPose();

        float forward = 1.01f;
        if (distance==-1) {
            forward = 0.76f;
        }

        final Vec3 TRANSLATION_OFFSET = new Vec3(0.5, 0.5, forward);
        matrixStack.translate(TRANSLATION_OFFSET.x, TRANSLATION_OFFSET.y, TRANSLATION_OFFSET.z); // translate

        matrixStack.scale(0.010416667F, -0.010416667F, 0.010416667F);

        int r = 255;
        int g = 255;
        int b = 0;
        Font fontrenderer = this.context.getFont(); //          X                  Y                COLOR
        fontrenderer.draw(matrixStack, "Dist: "+distance, 0.0f, 0.0f, NativeImage.combine(0, r, g, b));
        matrixStack.popPose();

        /*
        BlockRendererDispatcher blockRenderer = mc().getBlockRenderer();
        Block block = Blocks.END_ROD;
        BlockState state = block.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP);
        IBakedModel model = blockRenderer.getBlockModel(state);
        blockRenderer.renderSingleBlock(state, matrixStack, renderBuffers, combinedLight, combinedOverlay);
         *//*

        //render force field
        if (distance>0 || true) { //we are active
            /*GlStateManager._enableBlend();
            GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GlStateManager._depthMask(false);
            GlStateManager._alphaFunc(GL11.GL_LESS, 1.0F);*//*

            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.5, 0.5);
            float scale = 1.0f;
            matrixStack.scale(scale, -scale, -scale);
            FORCE_FIELD_MODEL.renderToBuffer(tile, this, matrixStack, renderBuffers, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.popPose();

            /*GlStateManager._depthMask(true);

            GlStateManager._disableBlend();*//*
        }
    }

    // this should be true for tileentities which render globally (no render bounding box), such as beacons.
    @Override
    public boolean shouldRenderOffScreen(ForceTubeTileEntity tile)
    {
        return true;
    }

    public static void register() {
        //ClientRegistry.bindTileEntityRenderer(TileEntityTypeInit.FORCE_TUBE_TYPE, ForceTubeTileEntityRenderer::new);
        throw new RuntimeException("Haven't ported because isn't used");
    }


    private static final Logger LOGGER = LogManager.getLogger();
}
*/