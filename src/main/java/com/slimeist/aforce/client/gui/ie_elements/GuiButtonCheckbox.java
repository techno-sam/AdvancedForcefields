/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.slimeist.aforce.client.gui.ie_elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import com.slimeist.aforce.AdvancedForcefields;
import net.minecraft.util.text.ITextComponent;

public class GuiButtonCheckbox extends GuiButtonBoolean
{
	private static final ResourceLocation TEXTURE = AdvancedForcefields.getId("textures/gui/hud_elements.png");

	public GuiButtonCheckbox(int x, int y, ITextComponent name, boolean state, IIEPressable<GuiButtonState<Boolean>> handler)
	{
		super(x, y, 8, 8, name, state, TEXTURE, 0, 128, -1, handler);
	}

	@Override
	public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
	{
		super.render(transform, mouseX, mouseY, partialTicks);
		if(this.visible&&getState())
		{
			int color;
			if(!this.active)
				color = 0xA0A0A0;
			else if(this.isHovered)
				color = 0xfff78034;
			else
				color = 0xE0E0E0;
			drawCenteredString(transform, Minecraft.getInstance().font, "\u2714", x+width/2, y-2, color);
		}
	}
}
