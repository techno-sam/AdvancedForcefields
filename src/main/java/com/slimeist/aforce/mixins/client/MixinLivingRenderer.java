package com.slimeist.aforce.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
@SuppressWarnings("rawtypes")
public class MixinLivingRenderer extends EntityRenderer {

    protected MixinLivingRenderer(EntityRendererProvider.Context p_i46179_1_) {
        super(p_i46179_1_);
    }

    @SuppressWarnings("unchecked")
    @Inject(at = @At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V", ordinal=0), method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", cancellable = true)
    private void maybeBlockRendering(LivingEntity living, float p_225623_2_, float p_225623_3_, PoseStack p_225623_4_, MultiBufferSource p_225623_5_, int p_225623_6_, CallbackInfo ci) {
        if (living instanceof Player) {
            if (MiscUtil.isPlayerWearingFullShimmeringArmor((Player) living)) {
                p_225623_4_.popPose();
                super.render(living, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<Player, PlayerModel<Player>>(living, (LivingEntityRenderer<Player, PlayerModel<Player>>) (Object) this, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_));
                ci.cancel();
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Entity p_110775_1_) {
        return null;
    }
}