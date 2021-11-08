/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.slimeist.aforce.client.gui.ie_elements;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiButtonBoolean extends GuiButtonState<Boolean>
{
	public GuiButtonBoolean(int x, int y, int w, int h, ITextComponent name, boolean state, ResourceLocation texture, int u, int v,
                            int offsetDir, IIEPressable<GuiButtonState<Boolean>> handler)
	{
		super(x, y, w, h, name, new Boolean[]{false, true}, state?1: 0, texture, u, v, offsetDir, handler);
	}
}