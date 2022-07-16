/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.slimeist.aforce.client.gui.ie_elements;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class GuiButtonBoolean extends GuiButtonState<Boolean>
{
	public GuiButtonBoolean(int x, int y, int w, int h, Component name, boolean state, ResourceLocation texture, int u, int v,
                            int offsetDir, IIEPressable<GuiButtonState<Boolean>> handler)
	{
		super(x, y, w, h, name, new Boolean[]{false, true}, state?1: 0, texture, u, v, offsetDir, handler);
	}
}