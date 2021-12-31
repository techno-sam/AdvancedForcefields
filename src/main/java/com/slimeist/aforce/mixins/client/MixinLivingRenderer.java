package com.slimeist.aforce.mixins.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingRenderer.class)
@SuppressWarnings("rawtypes")
public class MixinLivingRenderer extends EntityRenderer {

    protected MixinLivingRenderer(EntityRendererManager p_i46179_1_) {
        super(p_i46179_1_);
    }

    @SuppressWarnings("unchecked")
    @Inject(at = @At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/layers/LayerRenderer;render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/entity/Entity;FFFFFF)V", ordinal=0), method="render(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", cancellable = true)
    private void maybeBlockRendering(LivingEntity living, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, IRenderTypeBuffer p_225623_5_, int p_225623_6_, CallbackInfo ci) {
        if (living instanceof PlayerEntity) {
            if (MiscUtil.isPlayerWearingFullShimmeringArmor((PlayerEntity) living)) {
                p_225623_4_.popPose();
                super.render(living, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<PlayerEntity, PlayerModel<PlayerEntity>>(living, (LivingRenderer<PlayerEntity, PlayerModel<PlayerEntity>>) (Object) this, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_));
                ci.cancel();
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Entity p_110775_1_) {
        return null;
    }
}