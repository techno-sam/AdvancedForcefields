/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.slimeist.aforce.client.gui.ie_elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.slimeist.aforce.core.util.ColorUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.DyeColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class GuiButtonDyeColor extends GuiButtonState<DyeColor>
{
	public GuiButtonDyeColor(int x, int y, String name, DyeColor initialColor, GuiButtonIE.IIEPressable<GuiButtonState<DyeColor>> handler)
	{
		super(x, y, 8, 8, Component.nullToEmpty(name), DyeColor.values(), initialColor.ordinal(), GuiReactiveList.TEXTURE, 0, 128, -1, handler);
	}

	@Override
	public void render(PoseStack transform, int mouseX, int mouseY, float partialTicks)
	{
		super.render(transform, mouseX, mouseY, partialTicks);
		if(this.visible)
		{
			DyeColor dye = getState();
			int r = (int) (255 * dye.getTextureDiffuseColors()[0]);
			int g = (int) (255 * dye.getTextureDiffuseColors()[1]);
			int b = (int) (255 * dye.getTextureDiffuseColors()[2]);
			int col = 0xff000000|ColorUtil.packRGB(r, g, b);
			this.fillGradient(transform, x+2, y+2, x+6, y+6, col, col);
		}
	}
}
