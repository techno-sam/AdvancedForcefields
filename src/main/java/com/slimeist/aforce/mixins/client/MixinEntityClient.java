package com.slimeist.aforce.mixins.client;

import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntityClient {

    @Inject(at = @At(value="RETURN"), method="isInvisibleTo", cancellable = true)
    private void isInvisibleTo(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player != null && MiscUtil.isPlayerWearingShimmeringHelmet(player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At(value="RETURN"), method="isInvisible", cancellable = true)
    private void isInvisible(CallbackInfoReturnable<Boolean> cir) {
        Entity this_entity = (Entity) (Object) this;
        if (this_entity instanceof Player && MiscUtil.isPlayerWearingFullShimmeringArmor((Player) this_entity)) {
            cir.setReturnValue(true);
        }
    }
}