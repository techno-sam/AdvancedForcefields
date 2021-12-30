package com.slimeist.aforce.mixins.client;

import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntityClient {

    @Inject(at = @At(value="RETURN"), method="isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z", cancellable = true)
    private void isInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (player != null && MiscUtil.isPlayerWearingShimmeringHelmet(player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At(value="RETURN"), method="isInvisible()Z", cancellable = true)
    private void isInvisible(CallbackInfoReturnable<Boolean> cir) {
        Entity this_entity = (Entity) (Object) this;
        if (this_entity instanceof PlayerEntity && MiscUtil.isPlayerWearingFullShimmeringArmor((PlayerEntity) this_entity)) {
            cir.setReturnValue(true);
        }
    }
}