/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.slimeist.aforce.client.gui.ie_elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.slimeist.aforce.client.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class GuiButtonState<E> extends GuiButtonIE
{
	public E[] states;
	private int state;
	protected final int offsetDir;
	public int[] textOffset = {0, 0};

	public GuiButtonState(int x, int y, int w, int h, Component name, E[] states, int initialState, ResourceLocation texture, int u,
						  int v, int offsetDir, IIEPressable<GuiButtonState<E>> handler)
	{
		super(x, y, w, h, name, texture, u, v, handler);
		this.states = states;
		this.state = initialState;
		this.offsetDir = offsetDir;
		textOffset = new int[]{width+1, height/2-3};
	}

	protected int getNextStateInt()
	{
		return (state+1)%states.length;
	}

	public E getNextState()
	{
		return this.states[getNextStateInt()];
	}

	public void setStateByInt(int state)
	{
		this.state = state;
	}

	public E getState()
	{
		return this.states[this.state];
	}

	protected int getStateAsInt()
	{
		return this.state;
	}

	public int[] getTextOffset(Font fontrenderer)
	{
		return this.textOffset;
	}

	@Override
	public void render(PoseStack transform, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		if(this.visible)
		{
			ClientUtils.bindTexture(texture);
			Font fontrenderer = mc.font;
			this.isHovered = mouseX >= this.x&&mouseY >= this.y&&mouseX < this.x+this.width&&mouseY < this.y+this.height;
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(770, 771, 1, 0);
			RenderSystem.blendFunc(770, 771);
			int u = texU+(offsetDir==0?width: offsetDir==2?-width: 0)*state;
			int v = texV+(offsetDir==1?height: offsetDir==3?-height: 0)*state;
			this.blit(transform, x, y, u, v, width, height);
			if(!getMessage().getString().isEmpty())
			{
				int txtCol = 0xE0E0E0;
				if(!this.active)
					txtCol = 0xA0A0A0;
				else if(this.isHovered)
					txtCol = 0xfff78034;
				int[] offset = getTextOffset(fontrenderer);
				drawString(transform, fontrenderer, getMessage(), x+offset[0], y+offset[1], txtCol);
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int key)
	{
		boolean b = super.mouseClicked(mouseX, mouseY, key);
		if(b)
			this.state = getNextStateInt();
		return b;
	}
}
