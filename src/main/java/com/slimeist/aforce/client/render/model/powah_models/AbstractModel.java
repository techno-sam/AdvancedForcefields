package com.slimeist.aforce.client.render.model.powah_models;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public abstract class AbstractModel<T extends BlockEntity, R extends BlockEntityRenderer<T>> extends EmptyModel {
    public AbstractModel(Function<ResourceLocation, RenderType> function) {
        super(function);
    }

    public abstract void renderToBuffer(T te, R renderer, PoseStack matrix, MultiBufferSource rtb, int light, int ov);
    public abstract void renderToBuffer(T te, R renderer, PoseStack matrix, MultiBufferSource rtb, int light, int ov, float red, float green, float blue, float alpha);

    protected void setRotation(ModelPart model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}
