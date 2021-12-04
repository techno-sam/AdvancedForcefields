package com.slimeist.aforce.core.util;

import java.awt.*;

public class ColorUtil {

    public static int packRGB(int r, int g, int b) {
        return new Color(r, g, b).getRGB();
    }

    public static int[] unpackRGB(int rgb) {
        Color c = new Color(rgb);
        return new int[]{c.getRed(), c.getGreen(), c.getBlue()};
    }

    public static int packRGBA(int r, int g, int b, int a) {
        return new Color(r, g, b, a).getRGB();
    }

    public static int[] unpackRGBA(int rgba) {
        Color c = new Color(rgba, true);
        return new int[]{c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()};
    }

}
