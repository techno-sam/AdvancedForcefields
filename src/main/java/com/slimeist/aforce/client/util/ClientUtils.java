package com.slimeist.aforce.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class ClientUtils {
    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static void bindTexture(ResourceLocation texture) {
        RenderSystem.setShaderTexture(0, texture);
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
