package com.slimeist.aforce.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class ClientUtils {
    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static void bindTexture(ResourceLocation texture) {
        mc().getTextureManager().bind(texture);
    }
}
