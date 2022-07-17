package com.slimeist.aforce.mixins.client;

import com.slimeist.aforce.client.util.ClientUtils;
import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(at = @At(value="RETURN"), method="shouldEntityAppearGlowing", cancellable = true)
    private void shouldGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = ClientUtils.mc().player;
        if (player != null && entity.isInvisible() && MiscUtil.isPlayerWearingFullShimmeringArmor(player) && player.isShiftKeyDown()) {
            cir.setReturnValue(true);
        }
    }
}