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

    public static double modDouble(double x, int modulus) {
        while (x>modulus) {
            x -= modulus;
        }
        while (x<-modulus) {
            x += modulus;
        }
        return x;
    }
}
