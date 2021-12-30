package com.slimeist.aforce.mixins.client;

import com.slimeist.aforce.client.util.ClientUtils;
import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(at = @At(value="RETURN"), method="shouldEntityAppearGlowing(Lnet/minecraft/entity/Entity;)Z", cancellable = true)
    private void shouldGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ClientPlayerEntity player = ClientUtils.mc().player;
        if (player != null && entity.isInvisible() && MiscUtil.isPlayerWearingFullShimmeringArmor(player) && player.isShiftKeyDown()) {
            cir.setReturnValue(true);
        }
    }
}